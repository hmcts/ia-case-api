package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_SUBMITTED;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class FtpaDecisionMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && (callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final String ftpaApplicantType = asylumCase
            .read(FTPA_APPLICANT_TYPE, String.class)
            .orElseThrow(() -> new IllegalStateException("FtpaApplicantType is not present"));

        Optional<String> ftpaSubmitted = asylumCase.read(
            ftpaApplicantType.equals("appellant") == true ? FTPA_APPELLANT_SUBMITTED : FTPA_RESPONDENT_SUBMITTED, String.class);

        final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
        if (callback.getCaseDetails().getState() == State.FTPA_SUBMITTED || callback.getCaseDetails().getState() == State.FTPA_DECIDED) {
            if ((callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION)
                && ftpaApplicantType.equals("appellant") && !ftpaSubmitted.isPresent()) {

                asylumCasePreSubmitCallbackResponse.addError("You've made an invalid request. There is no appellant FTPA application to record the decision.");
                return asylumCasePreSubmitCallbackResponse;
            } else if ((callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION)
                       && ftpaApplicantType.equals("respondent") && !ftpaSubmitted.isPresent()) {

                asylumCasePreSubmitCallbackResponse.addError("You've made an invalid request. There is no respondent FTPA application to record the decision.");
                return asylumCasePreSubmitCallbackResponse;
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
