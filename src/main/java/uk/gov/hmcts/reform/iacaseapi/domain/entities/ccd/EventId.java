package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

public enum EventId {

    START_APPEAL("startAppeal"),
    CHANGE_APPEAL("changeAppeal"),
    SUBMIT_APPEAL("submitAppeal"),
    EDIT_GROUNDS_OF_APPEAL("editGroundsOfAppeal"),
    BUILD_APPEAL("buildAppeal"),
    UPLOAD_HOME_OFFICE_EVIDENCE("uploadHomeOfficeEvidence"),
    UPLOAD_DOCUMENT("uploadDocument"),
    SEND_HOME_OFFICE_EVIDENCE_DIRECTION("sendHomeOfficeEvidenceDirection"),
    SEND_BUILD_APPEAL_DIRECTION("sendBuildAppealDirection"),
    SEND_HOME_OFFICE_REVIEW_DIRECTION("sendHomeOfficeReviewDirection"),
    SEND_DIRECTION("sendDirection"),
    COMPLETE_DIRECTION("completeDirection"),
    REQUEST_TIME_EXTENSION("requestTimeExtension"),
    REVIEW_TIME_EXTENSION("reviewTimeExtension"),
    ADD_CASE_NOTE("addCaseNote"),
    ADD_HOME_OFFICE_RESPONSE("addHomeOfficeResponse"),
    PROCEED_TO_HEARING("proceedToHearing"),
    RECORD_LISTING("recordListing"),
    CREATE_HEARING_SUMMARY("createHearingSummary"),
    CREATE_HEARING_READY_BUNDLE("createHearingReadyBundle");

    private final String id;

    EventId(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }
}
