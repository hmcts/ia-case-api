package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

public enum EventId {

    START_DRAFT_APPEAL("startDraftAppeal"),
    COMPLETE_DRAFT_APPEAL("completeDraftAppeal"),
    UPDATE_DRAFT_APPEAL("updateDraftAppeal"),
    SUBMIT_APPEAL("submitAppeal");

    private final String id;

    EventId(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }
}
