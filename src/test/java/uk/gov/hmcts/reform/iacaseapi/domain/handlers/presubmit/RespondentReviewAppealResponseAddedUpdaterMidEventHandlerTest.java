package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.END_APPEAL_OUTCOME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.END_APPEAL_OUTCOME_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaResidentJudgeDecisionOutcomeType.WITHDRAWN;

public class RespondentReviewAppealResponseAddedUpdaterMidEventHandlerTest {

    private RespondentReviewAppealResponseAddedUpdaterMidEventHandler handler;

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new RespondentReviewAppealResponseAddedUpdaterMidEventHandler();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_write_reason_when_outcome_is_withdrawn() {
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        when(callback.getCaseDetails().getCaseData().read(END_APPEAL_OUTCOME, String.class))
                .thenReturn(Optional.of("withdrawn")); // case-insensitive

        PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(response.getData()).isEqualTo(asylumCase);
    }

    @Test
    void should_not_write_reason_for_non_withdrawn_outcome() {
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        when(callback.getCaseDetails().getCaseData().read(END_APPEAL_OUTCOME, String.class))
                .thenReturn(Optional.of("refused"));

        PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(response.getData()).isEqualTo(asylumCase);
    }

    @Test
    void can_handle_only_mid_event_end_appeal() {
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);

        assertThat(handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback)).isTrue();
        assertThat(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)).isFalse();
    }

    @Test
    void cannot_handle_other_events() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertThat(handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback)).isFalse();
    }

    @Test
    void should_throw_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot handle callback");
    }
}
