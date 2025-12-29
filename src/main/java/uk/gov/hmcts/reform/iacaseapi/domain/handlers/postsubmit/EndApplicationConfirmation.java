package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class EndApplicationConfirmation implements PostSubmitCallbackHandler<BailCase> {

    @Override
    public boolean canHandle(
        Callback<BailCase> callback
    ) {

        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.END_APPLICATION;
    }

    @Override
    public PostSubmitCallbackResponse handle(
        Callback<BailCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();
        postSubmitResponse.setConfirmationHeader("# You have ended the application");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
                + "A notification has been sent to all parties. "
                + "No further action is required.<br>"
        );
        return postSubmitResponse;
    }
}
