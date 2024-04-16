package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentTag {

    BAIL_EVIDENCE("uploadTheBailEvidenceDocs"),
    APPLICATION_SUBMISSION("applicationSubmission"),
    BAIL_SUMMARY("uploadBailSummary"),
    SIGNED_DECISION_NOTICE("signedDecisionNotice"),
    BAIL_DECISION_UNSIGNED("bailDecisionUnsigned"),
    UPLOAD_DOCUMENT("uploadDocument"),
    BAIL_SUBMISSION("bailSubmission"),
    B1_DOCUMENT("b1Document"),
    BAIL_END_APPLICATION("bailEndApplication"),
    BAIL_NOTICE_OF_HEARING("bailNoticeOfHearing"),

    @JsonEnumDefaultValue
    NONE("");

    @JsonValue
    private final String id;

    DocumentTag(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
