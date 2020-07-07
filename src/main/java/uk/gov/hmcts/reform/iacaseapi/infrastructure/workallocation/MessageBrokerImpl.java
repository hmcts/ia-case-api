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

            SendToWorkAllocationImpl.CamundaMappedObject camundaMappedObject = SendToWorkAllocationImpl.map(callback);

            message.setLongProperty("ccdId", camundaMappedObject.getCcdId());
            message.setStringProperty("event", camundaMappedObject.getEvent());
            message.setStringProperty("currentState", camundaMappedObject.getCurrentState());
            message.setStringProperty("previousState", camundaMappedObject.getPreviousStateString());
            message.setStringProperty("hearingCenter", camundaMappedObject.getHearingCentreString());
            message.setStringProperty("appellantName", camundaMappedObject.getAppellantName());
            message.setStringProperty("assignTo", camundaMappedObject.getAssignedTo());
            message.setStringProperty("dueDate", camundaMappedObject.getDueDate());
            message.setStringProperty("directionId", camundaMappedObject.getDirectionId());

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
                    new SendToWorkAllocationImpl.CamundaMappedObject(
                    message.getLongProperty("ccdId"),
                        message.getStringProperty("event"),
                        message.getStringProperty("currentState"),
                        message.getStringProperty("previousState"),
                        message.getStringProperty("hearingCenter"),
                        message.getStringProperty("appellantName"),
                        message.getStringProperty("assignTo"),
                        message.getStringProperty("dueDate"),
                        message.getStringProperty("directionId")
                    )
            );
            LOGGER.info("Sent event to send to Camunda");
        }
    }
}
