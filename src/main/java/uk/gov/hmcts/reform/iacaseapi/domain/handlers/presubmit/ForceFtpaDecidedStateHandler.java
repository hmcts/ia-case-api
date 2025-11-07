package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE35;

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
            String ftpaAppellantRjDecisionOutcomeType = asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class).orElse("");
            asylumCase.write(IS_FTPA_APPELLANT_DECIDED, "Yes");
            if (ftpaAppellantRjDecisionOutcomeType.equals(REHEARD_RULE35.toString())) {
                asylumCase.write(FTPA_FINAL_DECISION_FOR_DISPLAY, "reheardRule35");
            } else {
                asylumCase.write(FTPA_FINAL_DECISION_FOR_DISPLAY, "undecided");
            }
        } else if (appellantType.equals("respondent")) {
            String ftpaRespondentRjDecisionOutcomeType = asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class).orElse("");
            asylumCase.write(IS_FTPA_RESPONDENT_DECIDED, "Yes");
            if (ftpaRespondentRjDecisionOutcomeType.equals(REHEARD_RULE35.toString())) {
                asylumCase.write(FTPA_FINAL_DECISION_FOR_DISPLAY, "reheardRule35");
            } else {
                asylumCase.write(FTPA_FINAL_DECISION_FOR_DISPLAY, "undecided");
            }
        } else {
            throw new IllegalStateException("appellantType is is new and needs handling");
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
