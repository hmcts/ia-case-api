package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PostNotificationSender;

@Component
public class SendPostNotificationHandler implements PostSubmitCallbackHandler<AsylumCase> {

    private final PostNotificationSender<AsylumCase> postNotificationSender;

    public SendPostNotificationHandler(PostNotificationSender<AsylumCase> postNotificationSender) {
        this.postNotificationSender = postNotificationSender;
    }

    /**
     * Add the list of events to be handled in Post submit callback.
     * @return list of events to be handled.
     */
    private List<Event> getEventsToHandle() {
        return Arrays.asList(Event.APPLY_NOC_DECISION);
    }

    @Override
    public boolean canHandle(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        return getEventsToHandle().contains(callback.getEvent());
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<AsylumCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        PostSubmitCallbackResponse postSubmitCallbackResponse = postNotificationSender.send(callback);
        return postSubmitCallbackResponse;
    }
}
