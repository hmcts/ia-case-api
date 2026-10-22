package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMOVAL_OF_24W_DECISION_DECISION_MAKER;

@ExtendWith(MockitoExtension.class)
class RemoveStatutoryTimeframe24WeeksPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private UserDetails userDetails;
    @Mock private UserDetailsHelper userDetailsHelper;

    private RemoveStatutoryTimeframe24WeeksPreparer removeStatutoryTimeframe24WeeksPreparer;

    @BeforeEach
    void setUp() {
        removeStatutoryTimeframe24WeeksPreparer = new RemoveStatutoryTimeframe24WeeksPreparer(userDetails, userDetailsHelper);
    }

    @Test
    void canHandle_true() {
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        assertTrue(removeStatutoryTimeframe24WeeksPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"REMOVE_STATUTORY_TIMEFRAME_24_WEEKS"}, mode = EnumSource.Mode.EXCLUDE)
    void canHandle_false_invalid_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(removeStatutoryTimeframe24WeeksPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_START"}, mode = EnumSource.Mode.EXCLUDE)
    void canHandle_false_invalid_stage(PreSubmitCallbackStage stage) {
        assertFalse(removeStatutoryTimeframe24WeeksPreparer.canHandle(stage, callback));
    }

    @Test
    void canHandle_throws_null() {
        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> removeStatutoryTimeframe24WeeksPreparer.canHandle(null, callback));
        assertEquals("callbackStage must not be null", exception.getMessage());
        exception = assertThrows(NullPointerException.class,
            () -> removeStatutoryTimeframe24WeeksPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void handle_clears_fields() {
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.ADMIN_OFFICER);
        removeStatutoryTimeframe24WeeksPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        verify(asylumCase).clear(AsylumCaseFieldDefinition.REMOVAL_OF_24W_DECISION_REASON);
        verify(asylumCase).clear(REMOVAL_OF_24W_DECISION_DECISION_MAKER);
    }

    @ParameterizedTest
    @EnumSource(value = UserRoleLabel.class, names = {"JUDGE", "TRIBUNAL_CASEWORKER"})
    void handle_sets_decision_maker_if_valid_user_logged_in(UserRoleLabel userRoleLabel) {
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(userRoleLabel);
        when(userDetails.getForenameAndSurname()).thenReturn("Judge John Doe");
        removeStatutoryTimeframe24WeeksPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        verify(asylumCase).clear(AsylumCaseFieldDefinition.REMOVAL_OF_24W_DECISION_REASON);
        verify(asylumCase).write(REMOVAL_OF_24W_DECISION_DECISION_MAKER, "Judge John Doe");
        verify(asylumCase, never()).clear(REMOVAL_OF_24W_DECISION_DECISION_MAKER);
    }

    @ParameterizedTest
    @EnumSource(value = UserRoleLabel.class, names = {"JUDGE", "TRIBUNAL_CASEWORKER"}, mode = EnumSource.Mode.EXCLUDE)
    void handle_clears_decision_maker_if_non_valid_user_logged_in(UserRoleLabel userRoleLabel) {
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(userRoleLabel);
        removeStatutoryTimeframe24WeeksPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        verify(asylumCase).clear(AsylumCaseFieldDefinition.REMOVAL_OF_24W_DECISION_REASON);
        verify(asylumCase, never()).write(eq(REMOVAL_OF_24W_DECISION_DECISION_MAKER), anyString());
        verify(asylumCase).clear(REMOVAL_OF_24W_DECISION_DECISION_MAKER);
    }

    @Test
    void handle_throws_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> removeStatutoryTimeframe24WeeksPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
        assertEquals("Cannot handle callback", exception.getMessage());
    }
}