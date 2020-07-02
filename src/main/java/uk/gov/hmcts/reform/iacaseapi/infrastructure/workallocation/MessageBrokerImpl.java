package uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation;


import java.util.logging.Logger;
import javax.jms.*;
import lombok.SneakyThrows;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@Component
public class MessageBrokerImpl implements MessageBroker<AsylumCase> {
    private final static Logger LOGGER = Logger.getLogger(MessageBrokerImpl.class.getName());

    private final SendToWorkAllocationImpl sendToWorkAllocation;
    private final Session session;
    private final Queue queue;

    public MessageBrokerImpl(SendToWorkAllocationImpl sendToWorkAllocation) throws JMSException {
        this.sendToWorkAllocation = sendToWorkAllocation;
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");


        Connection connection = connectionFactory.createConnection();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = session.createQueue("sendToCamunda");
        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(new SendToCamundaListener());
        connection.start();
    }

    @Override
    public void sendToCamunda(Callback<AsylumCase> callback) {
        try {
            MessageProducer producer = session.createProducer(queue);
            Message message = session.createMessage();

            message.setLongProperty("ccdId", callback.getCaseDetails().getId());
            message.setStringProperty("event", callback.getEvent().toString());
            message.setStringProperty("currentState", callback.getCaseDetails().getState().toString());

            String previousStateString = callback.getCaseDetailsBefore().map(previousState -> {
                return previousState.getState().toString();
            }).orElse("");
            message.setStringProperty("previousState", previousStateString);

            AsylumCase caseData = callback.getCaseDetails().getCaseData();
            String hearingCentreString = caseData.<HearingCentre>read(AsylumCaseFieldDefinition.HEARING_CENTRE).map(hearingCentre -> {
                return hearingCentre.getValue();
            }).orElse("");
            message.setStringProperty("hearingCenter", hearingCentreString);
            message.setStringProperty("appellantName", caseData.<String>read(AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY).orElse(""));
            message.setStringProperty("assignTo", caseData.<String>read(AsylumCaseFieldDefinition.ASSIGNED_TO).orElse(null));

            LOGGER.info("Sending event to internal queue");
            producer.send(message);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private class SendToCamundaListener implements MessageListener {
        @SneakyThrows
        @Override
        public void onMessage(Message message) {
            LOGGER.info("Received event to send to Camunda");

            sendToWorkAllocation.createTask(
                    message.getLongProperty("ccdId"),
                    message.getStringProperty("event"),
                    message.getStringProperty("currentState"),
                    message.getStringProperty("previousState"),
                    message.getStringProperty("hearingCenter"),
                    message.getStringProperty("appellantName"),
                    message.getStringProperty("assignTo"));
            LOGGER.info("Sent event to send to Camunda");
        }
    }
}
