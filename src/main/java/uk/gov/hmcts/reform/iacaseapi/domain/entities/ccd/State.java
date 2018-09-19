package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

public enum State {

    APPEAL_STARTED("appealStarted"),
    APPEAL_SUBMITTED("appealSubmitted"),
    APPEAL_SENT_FOR_HOME_OFFICE_REVIEW("appealSentForHomeOfficeReview");

    private final String id;

    State(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }
}
