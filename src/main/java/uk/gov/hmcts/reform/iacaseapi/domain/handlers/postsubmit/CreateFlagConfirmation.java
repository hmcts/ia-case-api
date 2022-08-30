package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;

@Component
public class CreateFlagConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.CREATE_FLAG;
    }

    public PostSubmitCallbackResponse handle(
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        PostSubmitCallbackResponse postSubmitResponse =
                new PostSubmitCallbackResponse();
//        postSubmitResponse.setConfirmationHeader("# You've flagged this case");
//        postSubmitResponse.setConfirmationBody(
//                "#### What happens next\r\n\r\n"
//                        + "This flag will only be visible to the Tribunal. The case will proceed as usual."
//        );
        return postSubmitResponse;
    }
}
