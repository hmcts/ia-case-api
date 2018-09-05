package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

public enum EventId {

    START_DRAFT_APPEAL("startDraftAppeal"),
    COMPLETE_DRAFT_APPEAL("completeDraftAppeal"),
    UPDATE_DRAFT_APPEAL("updateDraftAppeal"),
    SUBMIT_APPEAL("submitAppeal"),
    UPDATE_SUMMARY("updateSummary"),
    SERVE_DIRECTION("serveDirection"),
    REQUEST_TIME_EXTENSION("requestTimeExtension"),
    REVIEW_TIME_EXTENSION("reviewTimeExtension"),
    MARK_READY_FOR_HOME_OFFICE("markReadyForHomeOffice");

    private final String id;

    EventId(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }
}
