package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECIDE_FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECIDE_FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPLICANT_TYPE;

@Component
public class DecideFtpaDecisionConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.DECIDE_FTPA_APPLICATION;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();


        final String ftpaApplicantType =
            asylumCase
                .read(FTPA_APPLICANT_TYPE, String.class)
                .orElseThrow(() -> new IllegalStateException("FtpaApplicantType is not present"));

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You've recorded the First-tier permission to appeal decision");

        String ftpaOutcomeType = asylumCase.read(ftpaApplicantType.equals("appellant") == true ? DECIDE_FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE : DECIDE_FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)
            .orElseThrow(() -> new IllegalStateException("ftpaDecisionOutcomeType is not present"));

        switch (ftpaOutcomeType) {

            case "granted":
            case "partiallyGranted":
                postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                    + "Both parties have been notified of the decision. The Upper Tribunal has also been notified, and will now proceed with the case.<br>"
                );
                break;

            case "refused":
                postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                    + "Both parties have been notified that permission was refused. They'll also be able to access this information in the FTPA tab.<br>"
                );
                break;

            case "reheardRule35":
                postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                    + "Both parties will be notified of the decision. A Caseworker will review any Tribunal instructions and then relist the case.<br>"
                );
                break;


            case "remadeRule31":
            case "remadeRule32":
                postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                    + "Both parties have been notified of the decision.<br>"
                );
                break;

            default:
                throw new IllegalStateException("FtpaDecisionOutcome is not present");
        }

        return postSubmitResponse;
    }
}

