package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingDecisionProcessor;

@Component
public class FtpaAppealDecisionStateHandler implements PreSubmitCallbackStateHandler<AsylumCase> {
    private final HearingDecisionProcessor hearingDecisionProcessor;

    public FtpaAppealDecisionStateHandler(HearingDecisionProcessor hearingDecisionProcessor) {
        this.hearingDecisionProcessor = hearingDecisionProcessor;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback,
        PreSubmitCallbackResponse<AsylumCase> callbackResponse
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final State currentState = callback.getCaseDetails().getState();

        hearingDecisionProcessor.processHearingFtpaAppellantDecision(asylumCase);

        if (currentState == State.FTPA_SUBMITTED || currentState == State.FTPA_DECIDED) {
            return new PreSubmitCallbackResponse<>(asylumCase, State.FTPA_DECIDED);
        }

        return new PreSubmitCallbackResponse<>(asylumCase, currentState);
    }
}
