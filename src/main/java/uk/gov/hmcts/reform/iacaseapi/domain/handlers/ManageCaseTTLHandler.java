package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TTL;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;


@Component
public class ManageCaseTTLHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_SUBMIT)
                && (callback.getEvent().equals(MANAGE_CASE_TTL)
                || callback.getEvent().equals(REINSTATE_APPEAL)
                || callback.getEvent().equals(RESIDENT_JUDGE_FTPA_DECISION)
                || callback.getEvent().equals(LEADERSHIP_JUDGE_FTPA_DECISION));
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

        TTL caseTypeTTL = asylumCase
                .read(CASE_TYPE_TTL, TTL.class)
                .orElseThrow(() -> new IllegalStateException("caseTypeTTL is not present"));

        if (callback.getEvent().equals(REINSTATE_APPEAL)) {
            caseTypeTTL.setSuspended(YesOrNo.YES);
            asylumCase.write(CASE_TYPE_TTL, caseTypeTTL);
        }

        if (callback.getEvent().equals(LEADERSHIP_JUDGE_FTPA_DECISION)) {
            String ftpaAppellantDecisionOutcomeType = asylumCase
                    .read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, String.class)
                    .orElseThrow(() -> new IllegalStateException("ftpaAppellantDecisionOutcomeType is not present"));

            if (ftpaAppellantDecisionOutcomeType.equals("granted")
                    || ftpaAppellantDecisionOutcomeType.equals("partiallyGranted")) {
                caseTypeTTL.setSuspended(YesOrNo.YES);
                asylumCase.write(CASE_TYPE_TTL, caseTypeTTL);
            }
        }

        if (callback.getEvent().equals(RESIDENT_JUDGE_FTPA_DECISION)) {
            String ftpaAppellantRjDecisionOutcomeType = asylumCase
                    .read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)
                    .orElseThrow(() -> new IllegalStateException("ftpaAppellantRjDecisionOutcomeType is not present"));

            if (ftpaAppellantRjDecisionOutcomeType.equals("granted")
                    || ftpaAppellantRjDecisionOutcomeType.equals("partiallyGranted")
                    || ftpaAppellantRjDecisionOutcomeType.equals("reheardRule35")
                    || ftpaAppellantRjDecisionOutcomeType.equals("reheardRule32")) {
                caseTypeTTL.setSuspended(YesOrNo.YES);
                asylumCase.write(CASE_TYPE_TTL, caseTypeTTL);
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
