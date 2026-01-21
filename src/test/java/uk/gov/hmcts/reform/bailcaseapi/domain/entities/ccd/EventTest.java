package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventTest {

    @Test
    void has_correct_values() {
        assertEquals("startApplication", Event.START_APPLICATION.toString());
        assertEquals("endApplication", Event.END_APPLICATION.toString());
        assertEquals("submitApplication", Event.SUBMIT_APPLICATION.toString());
        assertEquals("uploadBailSummary", Event.UPLOAD_BAIL_SUMMARY.toString());
        assertEquals("recordTheDecision", Event.RECORD_THE_DECISION.toString());
        assertEquals("uploadSignedDecisionNotice", Event.UPLOAD_SIGNED_DECISION_NOTICE.toString());
        assertEquals("uploadSignedDecisionNoticeConditionalGrant", Event.UPLOAD_SIGNED_DECISION_NOTICE_CONDITIONAL_GRANT.toString());
        assertEquals("addCaseNote", Event.ADD_CASE_NOTE.toString());
        assertEquals("moveApplicationToDecided", Event.MOVE_APPLICATION_TO_DECIDED.toString());
        assertEquals("uploadDocuments", Event.UPLOAD_DOCUMENTS.toString());
        assertEquals("sendBailDirection", Event.SEND_BAIL_DIRECTION.toString());
        assertEquals("editBailDocuments", Event.EDIT_BAIL_DOCUMENTS.toString());
        assertEquals("editBailApplication", Event.EDIT_BAIL_APPLICATION.toString());
        assertEquals("editBailApplicationAfterSubmit", Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT.toString());
        assertEquals("changeBailDirectionDueDate", Event.CHANGE_BAIL_DIRECTION_DUE_DATE.toString());
        assertEquals("makeNewApplication", Event.MAKE_NEW_APPLICATION.toString());
        assertEquals("viewPreviousApplications", Event.VIEW_PREVIOUS_APPLICATIONS.toString());
        assertEquals("nocRequest", Event.NOC_REQUEST.toString());
        assertEquals("applyNocDecision", Event.APPLY_NOC_DECISION.toString());
        assertEquals("stopLegalRepresenting", Event.STOP_LEGAL_REPRESENTING.toString());
        assertEquals("updateBailLegalRepDetails", Event.UPDATE_BAIL_LEGAL_REP_DETAILS.toString());
        assertEquals("nocRequestBail", Event.NOC_REQUEST_BAIL.toString());
        assertEquals("clearLegalRepresentativeDetails", Event.CLEAR_LEGAL_REPRESENTATIVE_DETAILS.toString());
        assertEquals("createBailCaseLink", Event.CREATE_BAIL_CASE_LINK.toString());
        assertEquals("maintainBailCaseLinks", Event.MAINTAIN_BAIL_CASE_LINKS.toString());
        assertEquals("updateInterpreterDetails", Event.UPDATE_INTERPRETER_DETAILS.toString());
        assertEquals("createFlag", Event.CREATE_FLAG.toString());
        assertEquals("confirmDetentionLocation", Event.CONFIRM_DETENTION_LOCATION.toString());
        assertEquals("caseListing", Event.CASE_LISTING.toString());
        assertEquals("imaStatus", Event.IMA_STATUS.toString());
        assertEquals("changeTribunalCentre", Event.CHANGE_TRIBUNAL_CENTRE.toString());
        assertEquals("testTimedEventSchedule", Event.TEST_TIMED_EVENT_SCHEDULE.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
        assertEquals("uploadHearingRecording", Event.UPLOAD_HEARING_RECORDING.toString());
    }

    @Test
    void fail_if_changes_needed_after_modifying_class() {
        assertEquals(37, Event.values().length);
    }
}
