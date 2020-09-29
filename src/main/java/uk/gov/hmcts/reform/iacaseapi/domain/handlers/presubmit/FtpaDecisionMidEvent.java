package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.APPELLANT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.RESPONDENT;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
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
            ftpaApplicantType.equals(APPELLANT.toString()) ? FTPA_APPELLANT_SUBMITTED : FTPA_RESPONDENT_SUBMITTED, String.class);

        final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);

        if (callback.getCaseDetails().getState() == State.FTPA_SUBMITTED || callback.getCaseDetails().getState() == State.FTPA_DECIDED) {
            if ((callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION)
                && ftpaApplicantType.equals(APPELLANT.toString()) && !ftpaSubmitted.isPresent()) {

                asylumCasePreSubmitCallbackResponse.addError("You've made an invalid request. There is no appellant FTPA application to record the decision.");
                return asylumCasePreSubmitCallbackResponse;
            } else if ((callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION)
                       && ftpaApplicantType.equals(RESPONDENT.toString()) && !ftpaSubmitted.isPresent()) {

                asylumCasePreSubmitCallbackResponse.addError("You've made an invalid request. There is no respondent FTPA application to record the decision.");
                return asylumCasePreSubmitCallbackResponse;
            }
        }

        // Leadership judge page visibility flags
        setFtpaDecisionReasonsNotesVisibility(asylumCase);

        // Resident judge page visibility flags
        setResidentJudgeDecisionPagesVisibility(asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void setResidentJudgeDecisionPagesVisibility(AsylumCase asylumCase) {

        final YesOrNo isFtpaAppellantNoticeOfDecisionSetAside = asylumCase.read(IS_FTPA_APPELLANT_NOTICE_OF_DECISION_SET_ASIDE, YesOrNo.class).orElse(YesOrNo.NO);
        final String ftpaAppellantRjDecisionOutcomeType = asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class).orElse("");

        final YesOrNo isFtpaRespondentNoticeOfDecisionSetAside = asylumCase.read(IS_FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE, YesOrNo.class).orElse(YesOrNo.NO);
        final String ftpaRespondentRjDecisionOutcomeType = asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class).orElse("");

        // Page 3
        setNoticeOfDecisionSetAsideVisibility(asylumCase, ftpaAppellantRjDecisionOutcomeType, ftpaRespondentRjDecisionOutcomeType);

        // Page 4
        setDecisionObjectionsPageVisibility(asylumCase, isFtpaAppellantNoticeOfDecisionSetAside, ftpaAppellantRjDecisionOutcomeType,
            isFtpaRespondentNoticeOfDecisionSetAside, ftpaRespondentRjDecisionOutcomeType);

        // Page 5
        setDecisionReasonsNotesVisibility(asylumCase, ftpaAppellantRjDecisionOutcomeType, ftpaRespondentRjDecisionOutcomeType);

        // Page 6
        setDecisionListingVisibility(asylumCase, ftpaAppellantRjDecisionOutcomeType, ftpaRespondentRjDecisionOutcomeType);
    }

    private void setDecisionListingVisibility(AsylumCase asylumCase, String ftpaAppellantRjDecisionOutcomeType, String ftpaRespondentRjDecisionOutcomeType) {

        if (ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE35.toString())
            || ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE32.toString())) {

            asylumCase.write(AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_LISTING_VISIBLE, YesOrNo.YES);
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_LISTING_VISIBLE, YesOrNo.NO);
        }

        if (ftpaRespondentRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE35.toString())
            || ftpaRespondentRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE32.toString())) {

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

    private void setFtpaDecisionReasonsNotesVisibility(AsylumCase asylumCase) {

        final String ftpaAppellantDecisionOutcomeType = asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, String.class).orElse("");
        final String ftpaRespondentDecisionOutcomeType = asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, String.class).orElse("");

        if (ftpaAppellantDecisionOutcomeType.equals(FtpaDecisionOutcomeType.GRANTED.toString())
            || ftpaAppellantDecisionOutcomeType.equals(FtpaDecisionOutcomeType.PARTIALLY_GRANTED.toString())) {

            asylumCase.write(AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_REASONS_VISIBLE, YesOrNo.YES);
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_REASONS_VISIBLE, YesOrNo.NO);
        }

        if (ftpaRespondentDecisionOutcomeType.equals(FtpaDecisionOutcomeType.GRANTED.toString())
            || ftpaRespondentDecisionOutcomeType.equals(FtpaDecisionOutcomeType.PARTIALLY_GRANTED.toString())) {

            asylumCase.write(AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_REASONS_VISIBLE, YesOrNo.YES);
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_REASONS_VISIBLE, YesOrNo.NO);
        }
    }

    private void setDecisionObjectionsPageVisibility(
        final AsylumCase asylumCase,
        final YesOrNo isFtpaAppellantNoticeOfDecisionSetAside,
        final String ftpaAppellantRjDecisionOutcomeType,
        final YesOrNo isFtpaRespondentNoticeOfDecisionSetAside,
        final String ftpaRespondentRjDecisionOutcomeType
    ) {

        if (isFtpaAppellantNoticeOfDecisionSetAside.equals(YesOrNo.YES)
            || ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE35.toString())) {

            asylumCase.write(AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_OBJECTIONS_VISIBLE, YesOrNo.YES);
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_OBJECTIONS_VISIBLE, YesOrNo.NO);
        }

        if (isFtpaRespondentNoticeOfDecisionSetAside.equals(YesOrNo.YES)
            || ftpaRespondentRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE35.toString())) {

            asylumCase.write(AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_OBJECTIONS_VISIBLE, YesOrNo.YES);
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_OBJECTIONS_VISIBLE, YesOrNo.NO);
        }
    }

    private void setNoticeOfDecisionSetAsideVisibility(
        final AsylumCase asylumCase,
        final String ftpaAppellantRjDecisionOutcomeType,
        final String ftpaRespondentRjDecisionOutcomeType
    ) {

        if (ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.GRANTED.toString())
            || ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.PARTIALLY_GRANTED.toString())
            || ftpaAppellantRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.REFUSED.toString())) {

            asylumCase.write(AsylumCaseFieldDefinition.FTPA_APPELLANT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.YES);
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.FTPA_APPELLANT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.NO);
        }

        if (ftpaRespondentRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.GRANTED.toString())
            || ftpaRespondentRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.PARTIALLY_GRANTED.toString())
            || ftpaRespondentRjDecisionOutcomeType.equals(FtpaResidentJudgeDecisionOutcomeType.REFUSED.toString())) {

            asylumCase.write(AsylumCaseFieldDefinition.FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.YES);
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.NO);
        }
    }
}
