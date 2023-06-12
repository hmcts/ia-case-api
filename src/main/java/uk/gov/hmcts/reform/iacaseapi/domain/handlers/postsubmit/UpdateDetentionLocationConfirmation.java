package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class UpdateDetentionLocationConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(
            Callback<AsylumCase> callback
    ) {

        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.UPDATE_DETENTION_LOCATION;
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

        postSubmitResponse.setConfirmationHeader("# You have update the detained location");
        postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                        + "The appeal will continue."
        );

        return postSubmitResponse;
    }
}
