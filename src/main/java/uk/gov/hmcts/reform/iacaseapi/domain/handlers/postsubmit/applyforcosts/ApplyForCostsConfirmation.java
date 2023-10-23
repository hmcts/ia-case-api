package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.applyforcosts;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class ApplyForCostsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(
            Callback<AsylumCase> callback
    ) {

        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.APPLY_FOR_COSTS;
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

        postSubmitResponse.setConfirmationHeader("# You've made a costs application'");
        postSubmitResponse.setConfirmationBody(
                "## What happens next\n\n"
                        + "Both you and the other party will receive an email notification confirming your application.\n\n"
                        + "The other party has 14 days to respond to the claim.\n\n"
                        + "If you have requested a hearing, the Tribunal will consider your request.\n\n"
                        + "You can review the details of your application in the [Costs tab](/cases/case-details/" + callback.getCaseDetails().getId() + "#Costs). "
        );

        return postSubmitResponse;
    }
}
