package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_RESPONSE_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.END_APPEAL_OUTCOME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.END_APPEAL_OUTCOME_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaResidentJudgeDecisionOutcomeType.WITHDRAWN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_REVIEW_APPEAL_RESPONSE_ADDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class RespondentReviewAppealResponseAddedUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final State caseState =
            callback
                .getCaseDetails()
                .getState();

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        if (caseState == State.RESPONDENT_REVIEW) {

            asylumCase.write(
                    RESPONDENT_REVIEW_APPEAL_RESPONSE_ADDED,
                    asylumCase.read(APPEAL_RESPONSE_AVAILABLE, YesOrNo.class).orElse(NO)
            );

            String endAppealOutcome =
                    asylumCase
                            .read(END_APPEAL_OUTCOME, String.class)
                            .orElseThrow(() -> new IllegalStateException("endAppealOutcome is not present"));
            if (WITHDRAWN.toString().equalsIgnoreCase(endAppealOutcome)) {
                asylumCase.write(END_APPEAL_OUTCOME_REASON,
                        "The Respondent has withdrawn the decision under appeal and invited the Tribunal to treat the appeal as withdrawn under Rule 17(2).\n\n"
                                + "Upon considering the documents in this appeal, the Tribunal is satisfied that there is no good reason not to treat the appeal as withdrawn.\n\n"
                                + "The appeal is at an end.\n\n"
                                + "Legal Officer\n\n"
                                + "PLEASE NOTE: This decision is made by a Legal Officer in exercise of a specified power granted by the Senior President of Tribunals under rules 3(1) and (2) of the Tribunals Procedure "
                                + "(First-tier Tribunal) (Immigration and Asylum Chamber) rules 2014.  Any Party may, within 14 days of the date of this decision, apply in writing to the Tribunal for the decision to be considered afresh by a judge under rule 3(4)."
                );
            }


        } else {
            asylumCase.clear(RESPONDENT_REVIEW_APPEAL_RESPONSE_ADDED);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
