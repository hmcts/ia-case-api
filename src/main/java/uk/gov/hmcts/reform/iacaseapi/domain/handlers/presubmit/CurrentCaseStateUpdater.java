package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class CurrentCaseStateUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        State currentCaseState = callback.getCaseDetails().getState();

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        boolean disableCaseOfficerChange = asylumCase.read(DISABLE_OVERVIEW_PAGE, String.class)
            .map(flag -> flag.equals("Yes"))
            .orElse(false);
        boolean isUnknown = asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.class)
            .map(state -> state.equals(State.UNKNOWN))
            .orElse(false);
        if (disableCaseOfficerChange && isUnknown) {
            // do not update Case Officer state unless certain action is taken
        } else {
            asylumCase.write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, currentCaseState);
        }

        asylumCase.write(CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE, currentCaseState);
        asylumCase.write(CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER, currentCaseState);
        asylumCase.write(CURRENT_CASE_STATE_VISIBLE_TO_RESPONDENT_OFFICER, currentCaseState);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
