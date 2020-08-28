package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CurrentCaseStateUpdaterTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    CurrentCaseStateUpdater currentCaseStateUpdater =
        new CurrentCaseStateUpdater();

    @Test
    void should_set_case_building_ready_for_submission_flag_to_yes() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        for (State state : State.values()) {

            when(caseDetails.getState()).thenReturn(state);

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                currentCaseStateUpdater
                    .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE, state);
            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, state);
            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER, state);
            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_APC, state);
            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_LART, state);
            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_POU, state);
            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_GENERIC, state);
            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, state);
            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_JUDGE, state);
            reset(asylumCase);
        }
    }

    @Test
    void should_not_set_case_officer_state_when_overview_disabled() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        for (State state : State.values()) {

            when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.class)).thenReturn(Optional.of(State.UNKNOWN));
            when(asylumCase.read(DISABLE_OVERVIEW_PAGE, String.class)).thenReturn(Optional.of("Yes"));

            when(caseDetails.getState()).thenReturn(state);

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                currentCaseStateUpdater
                    .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            verify(asylumCase, times(0)).write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, state);
            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE, state);
            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER, state);
            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_APC, state);
            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_LART, state);
            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_POU, state);
            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_GENERIC, state);
            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, state);
            verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_JUDGE, state);
            reset(asylumCase);
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> currentCaseStateUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = currentCaseStateUpdater.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> currentCaseStateUpdater.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> currentCaseStateUpdater.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> currentCaseStateUpdater.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> currentCaseStateUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
