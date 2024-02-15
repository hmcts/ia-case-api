package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaResidentJudgeDecisionOutcomeType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.APPELLANT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.RESPONDENT;

@Component
public class DecideFtpaApplicationMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
            && (callback.getEvent() == Event.DECIDE_FTPA_APPLICATION);
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
            ftpaApplicantType.equals(APPELLANT.toString()) ? FTPA_APPELLANT_SUBMITTED : FTPA_RESPONDENT_SUBMITTED, String.class);

        final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);

        if (callback.getCaseDetails().getState() == State.FTPA_SUBMITTED || callback.getCaseDetails().getState() == State.FTPA_DECIDED) {
            if (callback.getEvent() == Event.DECIDE_FTPA_APPLICATION
                && ftpaApplicantType.equals(APPELLANT.toString()) && ftpaSubmitted.isEmpty()) {
                asylumCasePreSubmitCallbackResponse.addError("You've made an invalid request. There is no appellant FTPA application to record the decision.");
                return asylumCasePreSubmitCallbackResponse;
            } else if (callback.getEvent() == Event.DECIDE_FTPA_APPLICATION
                && ftpaApplicantType.equals(RESPONDENT.toString()) && ftpaSubmitted.isEmpty()) {
                asylumCasePreSubmitCallbackResponse.addError("You've made an invalid request. There is no respondent FTPA application to record the decision.");
                return asylumCasePreSubmitCallbackResponse;
            }
        }

        setResidentJudgeDecisionPagesVisibility(asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void setResidentJudgeDecisionPagesVisibility(AsylumCase asylumCase) {

        final String ftpaAppellantRjDecisionOutcomeType = asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class).orElse("");

        final String ftpaRespondentRjDecisionOutcomeType = asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class).orElse("");

        // Page 4
        setNoticeOfDecisionSetAsideVisibility(asylumCase, ftpaAppellantRjDecisionOutcomeType, ftpaRespondentRjDecisionOutcomeType);

        // Page 5
        setDecisionReasonsNotesVisibility(asylumCase, ftpaAppellantRjDecisionOutcomeType, ftpaRespondentRjDecisionOutcomeType);

        // Page 6
        setDecisionListingVisibility(asylumCase, ftpaAppellantRjDecisionOutcomeType, ftpaRespondentRjDecisionOutcomeType);
    }

    private void setDecisionListingVisibility(AsylumCase asylumCase, String ftpaAppellantRjDecisionOutcomeType, String ftpaRespondentRjDecisionOutcomeType) {

        if (ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE35.toString())) {
            asylumCase.write(AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_LISTING_VISIBLE, YesOrNo.YES);
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_LISTING_VISIBLE, YesOrNo.NO);
        }

        if (ftpaRespondentRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE35.toString())) {
            asylumCase.write(AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_LISTING_VISIBLE, YesOrNo.YES);
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_LISTING_VISIBLE, YesOrNo.NO);
        }
    }

    private void setDecisionReasonsNotesVisibility(AsylumCase asylumCase, String ftpaAppellantRjDecisionOutcomeType, String ftpaRespondentRjDecisionOutcomeType) {

        if (ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.GRANTED.toString())
            || ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.PARTIALLY_GRANTED.toString())) {

            asylumCase.write(AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_REASONS_NOTES_VISIBLE, YesOrNo.YES);
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_REASONS_NOTES_VISIBLE, YesOrNo.NO);
        }

        if (ftpaRespondentRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.GRANTED.toString())
            || ftpaRespondentRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.PARTIALLY_GRANTED.toString())) {

            asylumCase.write(AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_REASONS_NOTES_VISIBLE, YesOrNo.YES);
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_REASONS_NOTES_VISIBLE, YesOrNo.NO);
        }
    }

    private void setNoticeOfDecisionSetAsideVisibility(
        final AsylumCase asylumCase,
        final String ftpaAppellantRjDecisionOutcomeType,
        final String ftpaRespondentRjDecisionOutcomeType
    ) {

        if (ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.GRANTED.toString())
            || ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.PARTIALLY_GRANTED.toString())
            || ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.REFUSED.toString())
            || ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.APPLICATION_NOT_ADMITTED.toString())
            || ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE35.toString())) {

            asylumCase.write(AsylumCaseFieldDefinition.FTPA_APPELLANT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.YES);
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.FTPA_APPELLANT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.NO);
        }

        if (ftpaRespondentRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.GRANTED.toString())
            || ftpaRespondentRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.PARTIALLY_GRANTED.toString())
            || ftpaRespondentRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.REFUSED.toString())
            || ftpaRespondentRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.APPLICATION_NOT_ADMITTED.toString())
            || ftpaRespondentRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE35.toString())) {

            asylumCase.write(AsylumCaseFieldDefinition.FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.YES);
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.NO);
        }
    }
}
