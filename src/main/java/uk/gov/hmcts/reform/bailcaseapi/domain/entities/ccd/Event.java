package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Event {

    START_APPLICATION("startApplication"),
    SUBMIT_APPLICATION("submitApplication"),
    END_APPLICATION("endApplication"),
    UPLOAD_BAIL_SUMMARY("uploadBailSummary"),
    RECORD_THE_DECISION("recordTheDecision"),
    UPLOAD_SIGNED_DECISION_NOTICE("uploadSignedDecisionNotice"),
    ADD_CASE_NOTE("addCaseNote"),
    SEND_BAIL_DIRECTION("sendBailDirection"),
    MOVE_APPLICATION_TO_DECIDED("moveApplicationToDecided"),
    UPLOAD_DOCUMENTS("uploadDocuments"),
    EDIT_BAIL_DOCUMENTS("editBailDocuments"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    Event(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
