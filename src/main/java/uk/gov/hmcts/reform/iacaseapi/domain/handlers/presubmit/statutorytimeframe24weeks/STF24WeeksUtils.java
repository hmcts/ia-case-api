package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PRE_CLARIFYING_STATE;

public class STF24WeeksUtils {

    private STF24WeeksUtils() {
    }

    /**
     * Returns the effective state for STF 24-week checks.
     * When the case is in a clarifying questions state, the effective state is
     * the pre-clarifying state (i.e. the state before clarifying questions were requested).
     */
    public static State getEffectiveState(Callback<AsylumCase> callback, AsylumCase asylumCase) {
        State currentState = callback.getCaseDetails().getState();
        if (currentState == State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS
            || currentState == State.CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED) {
            return asylumCase.read(PRE_CLARIFYING_STATE, State.class)
                .orElse(currentState);
        }
        return currentState;
    }
}
