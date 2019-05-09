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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class RespondentReviewAppealResponseAddedUpdaterTest {

    @Mock private Callback<CaseDataMap> callback;
    @Mock private CaseDetails<CaseDataMap> caseDetails;
    @Mock private CaseDataMap CaseDataMap;

    private RespondentReviewAppealResponseAddedUpdater respondentReviewAppealResponseAddedUpdater =
        new RespondentReviewAppealResponseAddedUpdater();

    @Before
    public void setUp() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(CaseDataMap);
        when(caseDetails.getState()).thenReturn(State.RESPONDENT_REVIEW);
    }

    @Test
    public void should_be_handled_late() {
        assertEquals(DispatchPriority.LATE, respondentReviewAppealResponseAddedUpdater.getDispatchPriority());
    }

    @Test
    public void should_set_action_available_flag_to_yes_when_state_applies_and_appeal_response_available() {

        for (State state : State.values()) {

            when(caseDetails.getState()).thenReturn(state);
            when(CaseDataMap.getAppealResponseAvailable()).thenReturn(Optional.of(YesOrNo.YES));

            PreSubmitCallbackResponse<CaseDataMap> callbackResponse =
                respondentReviewAppealResponseAddedUpdater
                    .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(CaseDataMap, callbackResponse.getData());

            if (state == State.RESPONDENT_REVIEW) {

                verify(CaseDataMap, times(1)).setRespondentReviewAppealResponseAdded(YesOrNo.YES);

            } else {
                verify(CaseDataMap, never()).setRespondentReviewAppealResponseAdded(YesOrNo.NO);
                verify(CaseDataMap, never()).setRespondentReviewAppealResponseAdded(YesOrNo.YES);
                verify(CaseDataMap, times(1)).clearRespondentReviewAppealResponseAdded();
            }

            reset(CaseDataMap);
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

                when(CaseDataMap.getAppealResponseAvailable()).thenReturn(appealResponseNotAvailableInidication);

                PreSubmitCallbackResponse<CaseDataMap> callbackResponse =
                    respondentReviewAppealResponseAddedUpdater
                        .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

                assertNotNull(callbackResponse);
                assertEquals(CaseDataMap, callbackResponse.getData());

                verify(CaseDataMap, times(1)).setRespondentReviewAppealResponseAdded(YesOrNo.NO);
                verify(CaseDataMap, never()).clearRespondentReviewAppealResponseAdded();

                reset(CaseDataMap);
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
