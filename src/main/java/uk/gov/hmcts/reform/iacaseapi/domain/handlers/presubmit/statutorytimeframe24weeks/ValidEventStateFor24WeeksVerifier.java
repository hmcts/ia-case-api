package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.EnumSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PRE_CLARIFYING_STATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

@Component
public class ValidEventStateFor24WeeksVerifier implements PreSubmitCallbackHandler<AsylumCase> {

    protected static final Set<State> INVALID_UPDATE_HEARING_REQUEST_STATES = EnumSet.of(
        State.AWAITING_RESPONDENT_EVIDENCE,
        State.CASE_BUILDING,
        State.CASE_UNDER_REVIEW,
        State.RESPONDENT_REVIEW,
        State.SUBMIT_HEARING_REQUIREMENTS,
        State.AWAITING_REASONS_FOR_APPEAL,
        State.REASONS_FOR_APPEAL_SUBMITTED,
        State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS,
        State.CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED
    );

    private static final Set<Event> VALID_EVENTS = EnumSet.of(
        UPDATE_HEARING_REQUEST,
        UPDATE_INTERPRETER_DETAILS,
        UPDATE_INTERPRETER_BOOKING_STATUS,
        ADJOURN_HEARING_WITHOUT_DATE,
        RECORD_ADJOURNMENT_DETAILS,
        UPDATE_HEARING_REQUIREMENTS,
        REVIEW_HEARING_REQUIREMENTS
    );

    private static final String STATE_ERROR =
        "This event cannot be run in this state";

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_START) && VALID_EVENTS.contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        State currentState = getEffectiveState(callback, asylumCase);

        boolean is24WeekCase = asylumCase.read(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED, YesOrNo.class)
            .orElse(YesOrNo.NO) == YesOrNo.YES;

        Event event = callback.getEvent();
        boolean invalidState = isInvalidState(event, currentState, is24WeekCase);

        if (invalidState) {
            return new PreSubmitCallbackResponse<>(asylumCase).withError(STATE_ERROR);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    public State getEffectiveState(Callback<AsylumCase> callback, AsylumCase asylumCase) {
        State currentState = callback.getCaseDetails().getState();
        if (currentState == State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS
            || currentState == State.CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED) {
            return asylumCase.read(PRE_CLARIFYING_STATE, State.class)
                .orElse(currentState);
        }
        return currentState;
    }

    public boolean isInvalidState(Event event, State state, boolean is24WeekCase) {
        if (event == UPDATE_HEARING_REQUEST) {
            return !is24WeekCase
                && INVALID_UPDATE_HEARING_REQUEST_STATES.contains(state);
        }

        return is24WeekCase
            ? state == State.LISTING
            : state == State.RESPONDENT_REVIEW;
    }
}
