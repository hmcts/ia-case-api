package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_RESPONSE_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.END_APPEAL_OUTCOME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaResidentJudgeDecisionOutcomeType.WITHDRAWN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_REVIEW_APPEAL_RESPONSE_ADDED;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RespondentReviewAppealResponseAddedUpdaterTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private String endAppealOutcome = WITHDRAWN.toString();

    private RespondentReviewAppealResponseAddedUpdater respondentReviewAppealResponseAddedUpdater =
        new RespondentReviewAppealResponseAddedUpdater();

    @BeforeEach
    public void setUp() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.RESPONDENT_REVIEW);
    }

    @Test
    void should_be_handled_late() {
        assertEquals(DispatchPriority.LATE, respondentReviewAppealResponseAddedUpdater.getDispatchPriority());
    }

    @Test
    void should_set_action_available_flag_to_yes_when_state_applies_and_appeal_response_available() {

        for (State state : State.values()) {

            when(caseDetails.getState()).thenReturn(state);
            when(asylumCase.read(APPEAL_RESPONSE_AVAILABLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
            when(asylumCase.read(END_APPEAL_OUTCOME, String.class)).thenReturn(Optional.of(endAppealOutcome));

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                respondentReviewAppealResponseAddedUpdater
                    .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            if (state == State.RESPONDENT_REVIEW) {

                verify(asylumCase, times(1)).write(RESPONDENT_REVIEW_APPEAL_RESPONSE_ADDED, YesOrNo.YES);

            } else {
                verify(asylumCase, never()).write(RESPONDENT_REVIEW_APPEAL_RESPONSE_ADDED, YesOrNo.NO);
                verify(asylumCase, never()).write(RESPONDENT_REVIEW_APPEAL_RESPONSE_ADDED, YesOrNo.YES);
                verify(asylumCase, times(1)).clear(RESPONDENT_REVIEW_APPEAL_RESPONSE_ADDED);
            }

            reset(asylumCase);
        }
    }

    @Test
    void should_set_action_available_flag_to_no_when_state_applies_and_appeal_response_not_available() {

        List<Optional<YesOrNo>> appealResponseNotAvailableIndications =
            Arrays.asList(
                Optional.empty(),
                Optional.of(YesOrNo.NO)
            );

        appealResponseNotAvailableIndications
            .forEach(appealResponseNotAvailableIndication -> {

                when(asylumCase.read(APPEAL_RESPONSE_AVAILABLE, YesOrNo.class))
                    .thenReturn(appealResponseNotAvailableIndication);
                when(asylumCase.read(END_APPEAL_OUTCOME, String.class))
                        .thenReturn(Optional.of(endAppealOutcome));

                PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                    respondentReviewAppealResponseAddedUpdater
                        .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

                assertNotNull(callbackResponse);
                assertEquals(asylumCase, callbackResponse.getData());

                verify(asylumCase, times(1)).write(RESPONDENT_REVIEW_APPEAL_RESPONSE_ADDED, YesOrNo.NO);
                verify(asylumCase, never()).clear(RESPONDENT_REVIEW_APPEAL_RESPONSE_ADDED);

                reset(asylumCase);
            });
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> respondentReviewAppealResponseAddedUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback_for_all_events() {

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
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> respondentReviewAppealResponseAddedUpdater.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> respondentReviewAppealResponseAddedUpdater.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> respondentReviewAppealResponseAddedUpdater.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> respondentReviewAppealResponseAddedUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
