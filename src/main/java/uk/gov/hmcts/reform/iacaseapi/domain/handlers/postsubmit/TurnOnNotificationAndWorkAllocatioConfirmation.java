package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;

@Component
public class TurnOnNotificationAndWorkAllocatioConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(
            Callback<AsylumCase> callback
    ) {

        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.TURN_ON_NOTIFICATIONS_WA_TASKS;
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

        postSubmitResponse.setConfirmationHeader("# You have turned on notifications/WA tasks");
        postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                        + "The rehydration process has now been completed and the case can be progressed."
        );

        return postSubmitResponse;
    }
}
