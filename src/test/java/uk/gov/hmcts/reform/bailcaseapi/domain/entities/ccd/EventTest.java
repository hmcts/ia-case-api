package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventTest {

    @ParameterizedTest
    @MethodSource("eventMapping")
    void has_correct_values(String expected, String actual) {
        assertEquals(expected, actual);
    }

    @Test
    void if_this_test_fails_it_is_because_eventMapping_needs_updating_with_your_changes() {
        List<String> eventMappingStrings = eventMapping().map(arg -> arg.get()[1])
            .map(String.class::cast)
            .toList();
        List<Event> missingEvents = Arrays.stream(Event.values())
            .filter(event -> !eventMappingStrings.contains(event.toString())).toList();
        assertTrue(missingEvents.isEmpty(), "The following events are missing from the eventMapping method: " + missingEvents);
    }

    static Stream<Arguments> eventMapping() {
        return Stream.of(
            Arguments.of("addCaseNote", Event.ADD_CASE_NOTE.toString()),
            Arguments.of("applyNocDecision", Event.APPLY_NOC_DECISION.toString()),
            Arguments.of("caseListing", Event.CASE_LISTING.toString()),
            Arguments.of("changeBailDirectionDueDate", Event.CHANGE_BAIL_DIRECTION_DUE_DATE.toString()),
            Arguments.of("clearLegalRepresentativeDetails", Event.CLEAR_LEGAL_REPRESENTATIVE_DETAILS.toString()),
            Arguments.of("confirmDetentionLocation", Event.CONFIRM_DETENTION_LOCATION.toString()),
            Arguments.of("createBailCaseLink", Event.CREATE_BAIL_CASE_LINK.toString()),
            Arguments.of("createFlag", Event.CREATE_FLAG.toString()),
            Arguments.of("editBailApplication", Event.EDIT_BAIL_APPLICATION.toString()),
            Arguments.of("editBailApplicationAfterSubmit", Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT.toString()),
            Arguments.of("editBailDocuments", Event.EDIT_BAIL_DOCUMENTS.toString()),
            Arguments.of("endApplication", Event.END_APPLICATION.toString()),
            Arguments.of("forceCaseToHearing", Event.FORCE_CASE_TO_HEARING.toString()),
            Arguments.of("imaStatus", Event.IMA_STATUS.toString()),
            Arguments.of("makeNewApplication", Event.MAKE_NEW_APPLICATION.toString()),
            Arguments.of("maintainBailCaseLinks", Event.MAINTAIN_BAIL_CASE_LINKS.toString()),
            Arguments.of("manageFlags", Event.MANAGE_FLAGS.toString()),
            Arguments.of("migrateWaBailApplication", Event.MIGRATE_WA_BAIL_APPLICATION.toString()),
            Arguments.of("moveApplicationToDecided", Event.MOVE_APPLICATION_TO_DECIDED.toString()),
            Arguments.of("nocRequest", Event.NOC_REQUEST.toString()),
            Arguments.of("nocRequestBail", Event.NOC_REQUEST_BAIL.toString()),
            Arguments.of("recordTheDecision", Event.RECORD_THE_DECISION.toString()),
            Arguments.of("removeBailLegalRepresentative", Event.REMOVE_BAIL_LEGAL_REPRESENTATIVE.toString()),
            Arguments.of("sendBailDirection", Event.SEND_BAIL_DIRECTION.toString()),
            Arguments.of("startApplication", Event.START_APPLICATION.toString()),
            Arguments.of("stopLegalRepresenting", Event.STOP_LEGAL_REPRESENTING.toString()),
            Arguments.of("submitApplication", Event.SUBMIT_APPLICATION.toString()),
            Arguments.of("updateBailLegalRepDetails", Event.UPDATE_BAIL_LEGAL_REP_DETAILS.toString()),
            Arguments.of("updateInterpreterBookingStatus", Event.UPDATE_INTERPRETER_BOOKING_STATUS.toString()),
            Arguments.of("updateInterpreterDetails", Event.UPDATE_INTERPRETER_DETAILS.toString()),
            Arguments.of("changeTribunalCentre", Event.CHANGE_TRIBUNAL_CENTRE.toString()),
            Arguments.of("uploadBailSummary", Event.UPLOAD_BAIL_SUMMARY.toString()),
            Arguments.of("uploadDocuments", Event.UPLOAD_DOCUMENTS.toString()),
            Arguments.of("uploadSignedDecisionNotice", Event.UPLOAD_SIGNED_DECISION_NOTICE.toString()),
            Arguments.of("uploadSignedDecisionNoticeConditionalGrant", Event.UPLOAD_SIGNED_DECISION_NOTICE_CONDITIONAL_GRANT.toString()),
            Arguments.of("viewPreviousApplications", Event.VIEW_PREVIOUS_APPLICATIONS.toString()),
            Arguments.of("saveNotificationsToDataBail", Event.SAVE_NOTIFICATIONS_TO_DATA_BAIL.toString()),
            Arguments.of("testTimedEventSchedule", Event.TEST_TIMED_EVENT_SCHEDULE.toString()),
            Arguments.of("regenerateBailSubmissionDocument", Event.REGENERATE_BAIL_SUBMISSION_DOCUMENT.toString()),
            Arguments.of("uploadHearingRecording", Event.UPLOAD_HEARING_RECORDING.toString()),
            Arguments.of("updateInterpreterWaTask", Event.UPDATE_INTERPRETER_WA_TASK.toString()),
            Arguments.of("rollbackMigration", Event.ROLLBACK_MIGRATION.toString()),
            Arguments.of("unknown", Event.UNKNOWN.toString())
        );
    }
}
