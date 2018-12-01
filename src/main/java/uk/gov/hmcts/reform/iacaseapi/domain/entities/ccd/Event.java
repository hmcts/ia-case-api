package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Event {

    START_APPEAL("startAppeal"),
    SUBMIT_APPEAL("submitAppeal"),
    SEND_DIRECTION("sendDirection"),
    REQUEST_RESPONDENT_EVIDENCE("requestRespondentEvidence"),
    UPLOAD_RESPONDENT_EVIDENCE("uploadRespondentEvidence"),
    BUILD_CASE("buildCase"),
    SUBMIT_CASE("submitCase"),
    REQUEST_RESPONDENT_REVIEW("requestRespondentReview"),
    ADD_APPEAL_RESPONSE("addAppealResponse"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    private final String id;

    Event(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
