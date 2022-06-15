package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EventTest {

    @Test
    void has_correct_values() {
        assertEquals("startApplication", Event.START_APPLICATION.toString());
        assertEquals("endApplication", Event.END_APPLICATION.toString());
        assertEquals("submitApplication", Event.SUBMIT_APPLICATION.toString());
        assertEquals("uploadBailSummary", Event.UPLOAD_BAIL_SUMMARY.toString());
        assertEquals("recordTheDecision", Event.RECORD_THE_DECISION.toString());
        assertEquals("uploadSignedDecisionNotice", Event.UPLOAD_SIGNED_DECISION_NOTICE.toString());
        assertEquals("addCaseNote", Event.ADD_CASE_NOTE.toString());
        assertEquals("moveApplicationToDecided", Event.MOVE_APPLICATION_TO_DECIDED.toString());
        assertEquals("uploadDocuments", Event.UPLOAD_DOCUMENTS.toString());
        assertEquals("sendBailDirection", Event.SEND_BAIL_DIRECTION.toString());
        assertEquals("editBailDocuments", Event.EDIT_BAIL_DOCUMENTS.toString());
        assertEquals("editBailApplication", Event.EDIT_BAIL_APPLICATION.toString());
        assertEquals("editBailApplicationAfterSubmit", Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT.toString());
        assertEquals("changeBailDirectionDueDate", Event.CHANGE_BAIL_DIRECTION_DUE_DATE.toString());
        assertEquals("makeNewApplication", Event.MAKE_NEW_APPLICATION.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
    }

    @Test
    void fail_if_changes_needed_after_modifying_class() {
        assertEquals(16, Event.values().length);
    }
}
