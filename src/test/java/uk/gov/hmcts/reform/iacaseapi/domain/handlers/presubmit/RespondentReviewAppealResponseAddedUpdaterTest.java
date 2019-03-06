package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
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
public class RespondentReviewAppealResponseAddedUpdaterTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private RespondentReviewAppealResponseAddedUpdater respondentReviewAppealResponseAddedUpdater =
        new RespondentReviewAppealResponseAddedUpdater();

    @Before
    public void setUp() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.RESPONDENT_REVIEW);
    }

    @Test
    public void should_set_action_available_flag_to_yes_when_state_applies_and_appeal_response_available() {

        for (State state : State.values()) {

            when(caseDetails.getState()).thenReturn(state);
            when(asylumCase.getAppealResponseAvailable()).thenReturn(Optional.of(YesOrNo.YES));

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                respondentReviewAppealResponseAddedUpdater
                    .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            if (state == State.RESPONDENT_REVIEW) {

                verify(asylumCase, times(1)).setRespondentReviewAppealResponseAdded(YesOrNo.YES);

            } else {
                verify(asylumCase, never()).setRespondentReviewAppealResponseAdded(YesOrNo.NO);
                verify(asylumCase, never()).setRespondentReviewAppealResponseAdded(YesOrNo.YES);
                verify(asylumCase, times(1)).clearRespondentReviewAppealResponseAdded();
            }

            reset(asylumCase);
        }
    }

    @Test
    public void should_set_action_available_flag_to_no_when_state_applies_and_appeal_response_not_available() {

        List<Optional<YesOrNo>> appealResponseNotAvailableInidications =
            Arrays.asList(
                Optional.empty(),
                Optional.of(YesOrNo.NO)
            );

        appealResponseNotAvailableInidications
            .forEach(appealResponseNotAvailableInidication -> {

                when(asylumCase.getAppealResponseAvailable()).thenReturn(appealResponseNotAvailableInidication);

                PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                    respondentReviewAppealResponseAddedUpdater
                        .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

                assertNotNull(callbackResponse);
                assertEquals(asylumCase, callbackResponse.getData());

                verify(asylumCase, times(1)).setRespondentReviewAppealResponseAdded(YesOrNo.NO);
                verify(asylumCase, never()).clearRespondentReviewAppealResponseAdded();

                reset(asylumCase);
            });
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> respondentReviewAppealResponseAddedUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = respondentReviewAppealResponseAddedUpdater.canHandle(callbackStage, callback);

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

        assertThatThrownBy(() -> respondentReviewAppealResponseAddedUpdater.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> respondentReviewAppealResponseAddedUpdater.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> respondentReviewAppealResponseAddedUpdater.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> respondentReviewAppealResponseAddedUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
