package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class SendDirectionActionAvailableUpdaterTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private SendDirectionActionAvailableUpdater sendDirectionActionAvailableUpdater =
        new SendDirectionActionAvailableUpdater();

    @Test
    public void should_set_action_available_flag_to_yes_when_state_applies() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        for (State state : State.values()) {

            when(caseDetails.getState()).thenReturn(state);

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                sendDirectionActionAvailableUpdater
                    .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            if (Arrays.asList(
                State.APPEAL_SUBMITTED,
                State.APPEAL_SUBMITTED_OUT_OF_TIME,
                State.AWAITING_RESPONDENT_EVIDENCE,
                State.CASE_BUILDING,
                State.CASE_UNDER_REVIEW,
                State.RESPONDENT_REVIEW,
                State.SUBMIT_HEARING_REQUIREMENTS,
                State.LISTING,
                State.PREPARE_FOR_HEARING,
                State.FINAL_BUNDLING,
                State.PRE_HEARING
            ).contains(state)) {

                verify(asylumCase, times(1)).setSendDirectionActionAvailable(YesOrNo.YES);
            } else {
                verify(asylumCase, times(1)).setSendDirectionActionAvailable(YesOrNo.NO);
            }

            reset(asylumCase);
        }
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> sendDirectionActionAvailableUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = sendDirectionActionAvailableUpdater.canHandle(callbackStage, callback);

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
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> sendDirectionActionAvailableUpdater.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendDirectionActionAvailableUpdater.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendDirectionActionAvailableUpdater.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sendDirectionActionAvailableUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
