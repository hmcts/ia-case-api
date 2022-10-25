package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.ccddataservice.TimeToLiveDataService;

@Component
public class ResidentJudgeFtpaDecisionConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private static final String GRANTED = "granted";
    private static final String PARTIALLY_GRANTED = "partiallyGranted";
    private static final String REFUSED = "refused";
    private static final String REHEARD_RULE_32 = "reheardRule32";
    private static final String REHEARD_RULE_35 = "reheardRule35";
    private static final String REMADE_RULE_32 = "remadeRule32";

    private final TimeToLiveDataService timeToLiveDataService;

    public ResidentJudgeFtpaDecisionConfirmation(TimeToLiveDataService timeToLiveDataService) {
        this.timeToLiveDataService = timeToLiveDataService;
    }

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION;
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

        String ftpaOutcomeType = asylumCase.read(ftpaApplicantType.equals("appellant") == true ? FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE : FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)
            .orElseThrow(() -> new IllegalStateException("ftpaDecisionOutcomeType is not present"));

        switch (ftpaOutcomeType) {

            case GRANTED:
            case PARTIALLY_GRANTED:
                postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                    + "Both parties have been notified of the decision. The Upper Tribunal has also been notified, and will now proceed with the case.<br>"
                );
                break;

            case REFUSED:
                postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                    + "Both parties have been notified that permission was refused. They'll also be able to access this information in the FTPA tab.<br>"
                );
                break;

            case REHEARD_RULE_32:
            case REHEARD_RULE_35:
                postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                    + "Both parties will be notified of the decision. A Caseworker will review any Tribunal instructions and then relist the case.<br>"
                );
                break;

            case REMADE_RULE_32:
                postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                    + "Both parties have been notified of the decision.<br>"
                );
                break;

            default:
                throw new IllegalStateException("FtpaDecisionOutcome is not present");
        }

        if (wasSuccessful(callback)
            && List.of(GRANTED, PARTIALLY_GRANTED, REHEARD_RULE_32, REHEARD_RULE_35).contains(ftpaOutcomeType)) {

            // stop the clock
            timeToLiveDataService.updateTheClock(callback, true);
        }

        return postSubmitResponse;
    }

    private boolean wasSuccessful(Callback<AsylumCase> callback) {
        CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        return caseDetails.getState().equals(State.FTPA_DECIDED);
    }
}

