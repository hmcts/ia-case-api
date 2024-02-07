package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class UpperTribunalBundlePreparer implements PreSubmitCallbackHandler<AsylumCase> {

    @Autowired
    public UpperTribunalBundlePreparer() {
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.GENERATE_UPPER_TRIBUNAL_BUNDLE;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Optional<String> ftpaRespondentRjDecisionOutcomeType =
            Optional.of(asylumCase.read(AsylumCaseFieldDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class).orElse(""));

        Optional<String> ftpaAppellantRjDecisionOutcomeType =
            Optional.of(asylumCase.read(AsylumCaseFieldDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class).orElse(""));

        boolean setAsideRespondentDecisionExists =
            ftpaRespondentRjDecisionOutcomeType.isPresent()
            && (ftpaRespondentRjDecisionOutcomeType.get().equals(DecideFtpaApplicationOutcomeType.REHEARD_RULE35.toString())
                || ftpaRespondentRjDecisionOutcomeType.get().equals(DecideFtpaApplicationOutcomeType.REHEARD_RULE32.toString())
                || ftpaRespondentRjDecisionOutcomeType.get().equals(DecideFtpaApplicationOutcomeType.REMADE_RULE32.toString()));

        boolean setAsideAppellantDecisionExists =
            ftpaAppellantRjDecisionOutcomeType.isPresent()
            && (ftpaAppellantRjDecisionOutcomeType.get().equals(DecideFtpaApplicationOutcomeType.REHEARD_RULE35.toString())
                || ftpaAppellantRjDecisionOutcomeType.get().equals(DecideFtpaApplicationOutcomeType.REHEARD_RULE32.toString())
                || ftpaAppellantRjDecisionOutcomeType.get().equals(DecideFtpaApplicationOutcomeType.REMADE_RULE32.toString()));

        if (setAsideRespondentDecisionExists || setAsideAppellantDecisionExists) {
            PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
            response.addError("You cannot generate an Upper Tribunal bundle because this appeal will not be heard by the Upper Tribunal.");
            return response;
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
