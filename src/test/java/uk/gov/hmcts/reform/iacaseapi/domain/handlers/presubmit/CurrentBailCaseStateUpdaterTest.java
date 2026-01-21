package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_APC;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_GENERIC;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_LART;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_POU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_JUDGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DISABLE_OVERVIEW_PAGE;

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
class CurrentBailCaseStateUpdaterTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private CurrentBailCaseStateUpdater currentBailCaseStateUpdater =
        new CurrentBailCaseStateUpdater();

    @Test
    void should_set_case_building_ready_for_submission_flag_to_yes() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        for (State state : State.values()) {

            when(caseDetails.getState()).thenReturn(state);

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                currentBailCaseStateUpdater
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

            when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.class))
                .thenReturn(Optional.of(State.UNKNOWN));
            when(asylumCase.read(DISABLE_OVERVIEW_PAGE, String.class)).thenReturn(Optional.of("Yes"));

            when(caseDetails.getState()).thenReturn(state);

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                currentBailCaseStateUpdater
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

        assertThatThrownBy(() -> currentBailCaseStateUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = currentBailCaseStateUpdater.canHandle(callbackStage, callback);

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

        assertThatThrownBy(() -> currentBailCaseStateUpdater.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> currentBailCaseStateUpdater.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> currentBailCaseStateUpdater.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> currentBailCaseStateUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
