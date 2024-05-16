package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.applyforcosts;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class DecideCostsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.DECIDE_COSTS_APPLICATION;
    }

    @Override
    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        String confirmationHeader = "# You have made a costs decision";
        String confirmationBody =
            "## What happens next\n\n"
                + "All parties have been notified.\n\n"
                + "The costs application is available to view in the [Costs tab](/cases/case-details/" + callback.getCaseDetails().getId() + "#Costs).";

        postSubmitResponse.setConfirmationHeader(confirmationHeader);
        postSubmitResponse.setConfirmationBody(confirmationBody);

        return postSubmitResponse;
    }
}
