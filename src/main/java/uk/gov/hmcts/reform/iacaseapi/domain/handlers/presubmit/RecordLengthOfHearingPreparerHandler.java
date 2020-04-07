package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_LENGTH_OF_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class RecordLengthOfHearingPreparerHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_START) && callback.getEvent().equals(RECORD_LENGTH_OF_HEARING);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback
            .getCaseDetails()
            .getCaseData();

        return asylumCase
            .read(FTPA_APPLICANT_TYPE, String.class)
            .flatMap(type ->
                asylumCase.read(
                    type.equals("appellant") ? FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE : FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE,
                    String.class
                )
            )
            .filter(outcome ->
                outcome.equals("reheardRule32") || outcome.equals("reheardRule35")
            )
            .map(outcome -> new PreSubmitCallbackResponse<>(asylumCase))
            .orElseGet(() -> {
                PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
                response.addError("Appeal can only be reheard due to appropriate Judge decision.");
                return response;
            });
    }

}
