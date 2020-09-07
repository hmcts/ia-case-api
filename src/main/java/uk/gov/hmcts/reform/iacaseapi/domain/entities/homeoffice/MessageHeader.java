package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

public class MessageHeader {

    private CodeWithDescription consumer;
    private String correlationId;
    private String eventDateTime;

    private MessageHeader() {

    }

    public MessageHeader(CodeWithDescription consumer, String correlationId, String eventDateTime) {
        this.consumer = consumer;
        this.correlationId = correlationId;
        this.eventDateTime = eventDateTime;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getEventDateTime() {
        return eventDateTime;
    }

    public CodeWithDescription getConsumer() {
        return consumer;
    }

}
