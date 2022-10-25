package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.TTL;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.ccddataservice.TimeToLiveDataService;

@Component
public class SendDecisionAndReasonsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final TimeToLiveDataService timeToLiveDataService;

    public SendDecisionAndReasonsConfirmation(TimeToLiveDataService timeToLiveDataService) {
        this.timeToLiveDataService = timeToLiveDataService;
    }

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.SEND_DECISION_AND_REASONS;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You've uploaded the Decision and Reasons document");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "Both parties have been notified of the decision. They'll also be able to access the Decision and Reasons document from the Documents tab."
        );

        // CCD doesn't set the "ttl.suspended" to NO (clock is active) unless it's null.
        // When the clock has been stopped "ttl.suspended" gets populated with "YES" and isn't null anymore
        // So it requires manual intervention to set "ttl.suspended = NO" when starting the clock again
        if (wasSuccessful(callback) && !isClockActive(callback.getCaseDetails().getCaseData())) {
            // stop the clock
            timeToLiveDataService.updateTheClock(callback, false);
        }

        return postSubmitResponse;
    }

    private boolean wasSuccessful(Callback<AsylumCase> callback) {
        CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        return caseDetails.getState().equals(State.DECIDED);
    }

    private boolean isClockActive(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseFieldDefinition.TTL, TTL.class)
            .map(ttl -> ttl.getSuspended().equals(YesOrNo.NO))
            .orElse(false);
    }
}
