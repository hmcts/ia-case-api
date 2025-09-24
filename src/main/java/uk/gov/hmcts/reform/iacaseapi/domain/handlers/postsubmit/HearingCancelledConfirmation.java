package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PostNotificationSender;

import static java.util.Objects.requireNonNull;

@Component
public class HearingCancelledConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final PostNotificationSender<AsylumCase> postNotificationSender;

    public HearingCancelledConfirmation(
            PostNotificationSender<AsylumCase> postNotificationSender
    ) {
        this.postNotificationSender = postNotificationSender;
    }

    public boolean canHandle(
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.HEARING_CANCELLED;
    }

    public PostSubmitCallbackResponse handle(
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        postNotificationSender.send(callback);

        PostSubmitCallbackResponse postSubmitResponse = new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# Hearing details updated");
        postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                            + "Add new hearing information as required."
        );

        return postSubmitResponse;
    }

}
