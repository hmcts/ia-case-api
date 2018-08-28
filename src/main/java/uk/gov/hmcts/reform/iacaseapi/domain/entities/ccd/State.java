package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

public enum State {

    DRAFT_APPEAL_STARTED("draftAppealStarted"),
    DRAFT_APPEAL_COMPLETED("draftAppealCompleted"),
    DRAFT_APPEAL_UPDATED("draftAppealUpdated"),
    APPEAL_SUBMITTED("appealSubmitted");

    private final String id;

    State(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }
}
