package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_RESPONSE_AVAILABLE;
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

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private RespondentReviewAppealResponseAddedUpdater updater;

    @BeforeEach
    public void setUp() {
        updater = new RespondentReviewAppealResponseAddedUpdater();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.RESPONDENT_REVIEW);
    }

    @Test
    void should_be_handled_late() {
        assertEquals(DispatchPriority.LATE, updater.getDispatchPriority());
    }

    @Test
    void should_set_flag_to_yes_when_state_is_respondent_review_and_appeal_response_available() {
        when(asylumCase.read(APPEAL_RESPONSE_AVAILABLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> response = updater.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        verify(asylumCase).write(RESPONDENT_REVIEW_APPEAL_RESPONSE_ADDED, YesOrNo.YES);
    }

    @Test
    void should_set_flag_to_no_when_state_is_respondent_review_and_appeal_response_not_available() {
        List<Optional<YesOrNo>> notAvailableOptions = Arrays.asList(Optional.empty(), Optional.of(YesOrNo.NO));

        for (Optional<YesOrNo> value : notAvailableOptions) {
            when(asylumCase.read(APPEAL_RESPONSE_AVAILABLE, YesOrNo.class)).thenReturn(value);

            PreSubmitCallbackResponse<AsylumCase> response = updater.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(response);
            verify(asylumCase).write(RESPONDENT_REVIEW_APPEAL_RESPONSE_ADDED, YesOrNo.NO);
            reset(asylumCase);
        }
    }

    @Test
    void should_clear_flag_when_not_in_respondent_review_state() {
        for (State state : State.values()) {
            if (state == State.RESPONDENT_REVIEW) continue;

            when(caseDetails.getState()).thenReturn(state);

            PreSubmitCallbackResponse<AsylumCase> response = updater.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(response);
            verify(asylumCase).clear(RESPONDENT_REVIEW_APPEAL_RESPONSE_ADDED);
            reset(asylumCase);
        }
    }

    @Test
    void can_handle_only_about_to_submit_stage() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
                boolean result = updater.canHandle(stage, callback);
                assertEquals(stage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT, result);
            }
        }
    }

    @Test
    void should_throw_on_invalid_handle_stage() {
        assertThatThrownBy(() -> updater.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> updater.canHandle(null, callback))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> updater.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("callback must not be null");

        assertThatThrownBy(() -> updater.handle(null, callback))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> updater.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("callback must not be null");
    }
}