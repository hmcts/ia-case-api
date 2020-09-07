package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

public class DecisionCommunication {

    private String description;
    private String dispatchDate;
    private String sentDate;
    private String type;

    private DecisionCommunication() {
    }

    public DecisionCommunication(String description, String dispatchDate, String sentDate, String type) {
        this.description = description;
        this.dispatchDate = dispatchDate;
        this.sentDate = sentDate;
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public String getDispatchDate() {
        return dispatchDate;
    }

    public String getSentDate() {
        return sentDate;
    }

    public String getType() {
        return type;
    }
}
