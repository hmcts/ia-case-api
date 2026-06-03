package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.END_APPEAL_OUTCOME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.END_APPEAL_OUTCOME_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaResidentJudgeDecisionOutcomeType.WITHDRAWN;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class RespondentReviewAppealResponseAddedUpdaterMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(PreSubmitCallbackStage stage, Callback<AsylumCase> callback) {
        requireNonNull(stage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return stage == PreSubmitCallbackStage.MID_EVENT
            && callback.getEvent() == uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.END_APPEAL;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage stage, Callback<AsylumCase> callback) {
        if (!canHandle(stage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        String endOutcomeReason = """
            The Respondent has withdrawn the decision under appeal and invited the Tribunal to treat the appeal as withdrawn under Rule 17(2).
            
            Upon considering the documents in this appeal, the Tribunal is satisfied that there is no good reason not to treat the appeal as withdrawn.
            
            The appeal is at an end.
            
            Legal Officer
            
            PLEASE NOTE: This decision is made by a Legal Officer in exercise of a specified power granted by the Senior President of Tribunals under rules 3(1) and (2) of the Tribunals Procedure \
            (First-tier Tribunal) (Immigration and Asylum Chamber) rules 2014.  Any Party may, within 14 days of the date of this decision, apply in writing to the Tribunal for the decision to be considered afresh by a judge under rule 3(4).""";
        asylumCase.read(END_APPEAL_OUTCOME, String.class)
            .filter(outcome -> outcome.equalsIgnoreCase(WITHDRAWN.toString()))
            .ifPresentOrElse(
                outcome -> {
                    if (State.RESPONDENT_REVIEW.equals(callback.getCaseDetails().getState())) {
                        asylumCase.write(END_APPEAL_OUTCOME_REASON, endOutcomeReason);
                    } else {
                        asylumCase.write(END_APPEAL_OUTCOME_REASON, "");
                    }
                },
                () -> {
                    asylumCase.write(END_APPEAL_OUTCOME_REASON, "");
                }
            );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
