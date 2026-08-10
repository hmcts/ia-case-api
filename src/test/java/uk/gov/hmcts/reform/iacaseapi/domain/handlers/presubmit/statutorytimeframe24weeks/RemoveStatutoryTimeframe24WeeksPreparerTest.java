package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveStatutoryTimeframe24WeeksPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock AsylumCase asylumCase;

    private RemoveStatutoryTimeframe24WeeksPreparer removeStatutoryTimeframe24WeeksPreparer;

    @BeforeEach
    void setUp() {
        removeStatutoryTimeframe24WeeksPreparer = new RemoveStatutoryTimeframe24WeeksPreparer();
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
    void handle_clears_removalOf24wDecisionReason() {
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        removeStatutoryTimeframe24WeeksPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        verify(asylumCase).clear(AsylumCaseFieldDefinition.REMOVAL_OF_24W_DECISION_REASON);
    }

    @Test
    void handle_throws_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> removeStatutoryTimeframe24WeeksPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
        assertEquals("Cannot handle callback", exception.getMessage());
    }
}