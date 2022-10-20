package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

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
public class ReinstateAppealConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final TimeToLiveDataService timeToLiveDataService;

    public ReinstateAppealConfirmation(TimeToLiveDataService timeToLiveDataService) {
        this.timeToLiveDataService = timeToLiveDataService;
    }

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.REINSTATE_APPEAL;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# This appeal has been reinstated");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "The legal representative and the Home Office will be notified that the case has been reinstated.<br>"
        );

        if (wasSuccessful(callback)) {
            // stop the clock
            timeToLiveDataService.updateTheClock(callback, true);
        }

        return postSubmitResponse;
    }

    private boolean wasSuccessful(Callback<AsylumCase> callback) {
        CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        return !caseDetails.getState().equals(State.ENDED);
    }
}
