package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_FTPA_CURRENT_DECISION_TEXT;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CurrentFtpaDecisionResolver;

@Component
public class EditFtpaDecisionPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    static final String NO_FTPA_DECISION_ERROR = "There is no FTPA decision to update.";

    private static final Map<String, String> OUTCOME_LABELS = Map.of(
        "granted", "Permission granted",
        "partiallyGranted", "Permission partially granted",
        "refused", "Permission refused",
        "notAdmitted", "Application not admitted",
        "reheardRule35", "Review decision under rule 35 - Resident Judge only",
        "remadeRule31", "Dispose of application under rule 31",
        "remadeRule32", "Dispose of application under rule 32"
    );

    private final CurrentFtpaDecisionResolver currentFtpaDecisionResolver;

    public EditFtpaDecisionPreparer(CurrentFtpaDecisionResolver currentFtpaDecisionResolver) {
        this.currentFtpaDecisionResolver = currentFtpaDecisionResolver;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && callback.getEvent() == Event.UPDATE_FTPA_DECISION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        Optional<String> currentDecisionLabel = currentFtpaDecisionResolver.getApplicantType(asylumCase)
            .filter(applicantType -> currentFtpaDecisionResolver.isDecided(asylumCase, applicantType))
            .flatMap(applicantType -> currentFtpaDecisionResolver.getCurrentOutcome(asylumCase, applicantType))
            .map(OUTCOME_LABELS::get);

        if (currentDecisionLabel.isEmpty()) {
            response.addError(NO_FTPA_DECISION_ERROR);
            return response;
        }

        currentFtpaDecisionResolver.clearUpdateFtpaDecisionFields(asylumCase);

        asylumCase.write(UPDATE_FTPA_CURRENT_DECISION_TEXT, currentDecisionLabel.get());

        return response;
    }
}
