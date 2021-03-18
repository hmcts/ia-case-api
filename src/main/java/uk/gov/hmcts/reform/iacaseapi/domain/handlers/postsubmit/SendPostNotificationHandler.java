package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Lists;
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
     *
     * @return list of events to be handled.
     */
    private List<Event> getEventsToHandle() {
        return Lists.newArrayList(
            Event.APPLY_NOC_DECISION,
            Event.REMOVE_REPRESENTATION
        );
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
        return postNotificationSender.send(callback);
    }
}
