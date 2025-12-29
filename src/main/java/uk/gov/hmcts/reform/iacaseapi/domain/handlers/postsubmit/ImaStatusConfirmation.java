package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class ImaStatusConfirmation implements PostSubmitCallbackHandler<BailCase> {

    @Override
    public boolean canHandle(
        Callback<BailCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.IMA_STATUS;
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

        String confirmationHeader = "# IMA status updated";
        String confirmationBody =
            "## What happens next\n\n"
            + "No further action is required.\n\n";


        postSubmitResponse.setConfirmationHeader(confirmationHeader);
        postSubmitResponse.setConfirmationBody(confirmationBody);

        return postSubmitResponse;
    }
}
