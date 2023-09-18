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
    CHANGE_BAIL_DIRECTION_DUE_DATE("changeBailDirectionDueDate"),
    MOVE_APPLICATION_TO_DECIDED("moveApplicationToDecided"),
    UPLOAD_DOCUMENTS("uploadDocuments"),
    EDIT_BAIL_DOCUMENTS("editBailDocuments"),
    EDIT_BAIL_APPLICATION("editBailApplication"),
    EDIT_BAIL_APPLICATION_AFTER_SUBMIT("editBailApplicationAfterSubmit"),
    MAKE_NEW_APPLICATION("makeNewApplication"),
    VIEW_PREVIOUS_APPLICATIONS("viewPreviousApplications"),
    NOC_REQUEST("nocRequest"),
    APPLY_NOC_DECISION("applyNocDecision"),
    REMOVE_BAIL_LEGAL_REPRESENTATIVE("removeBailLegalRepresentative"),
    STOP_LEGAL_REPRESENTING("stopLegalRepresenting"),
    UPDATE_BAIL_LEGAL_REP_DETAILS("updateBailLegalRepDetails"),
    NOC_REQUEST_BAIL("nocRequestBail"),
    CLEAR_LEGAL_REPRESENTATIVE_DETAILS("clearLegalRepresentativeDetails"),
    CREATE_BAIL_CASE_LINK("createBailCaseLink"),
    MAINTAIN_BAIL_CASE_LINKS("maintainBailCaseLinks"),
    CREATE_FLAG("createFlag"),

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
