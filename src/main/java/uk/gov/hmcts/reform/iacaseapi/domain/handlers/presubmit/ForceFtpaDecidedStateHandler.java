package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_APPELLANT_DECIDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FTPA_RESPONDENT_DECIDED;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;


@Component
public class ForceFtpaDecidedStateHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.FORCE_FTPA_DECIDED_STATE);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String appellantType = asylumCase.read(FTPA_APPLICANT_TYPE, String.class)
            .orElseThrow(() -> new IllegalStateException("appellantType is missing"));

        if (appellantType.equals("appellant")) {
            asylumCase.write(IS_FTPA_APPELLANT_DECIDED, "Yes");
        } else if (appellantType.equals("respondent")) {
            asylumCase.write(IS_FTPA_RESPONDENT_DECIDED, "Yes");

            String ftpaAppellantDecisionDate = asylumCase.read(FTPA_APPELLANT_DECISION_DATE, String.class)
                .orElseThrow(() -> new IllegalStateException("ftpaAppellantDecisionDate is missing"));
            asylumCase.write(FTPA_RESPONDENT_DECISION_DATE, ftpaAppellantDecisionDate);
        } else {
            throw new IllegalStateException("appellantType " + appellantType + " is new and needs handling");
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
