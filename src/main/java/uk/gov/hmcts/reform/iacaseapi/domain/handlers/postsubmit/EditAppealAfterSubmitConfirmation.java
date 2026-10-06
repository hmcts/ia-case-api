package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Component
public class EditAppealAfterSubmitConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return List.of(Event.EDIT_APPEAL_AFTER_SUBMIT, Event.EDIT_APPELLANT_PERSONAL_DATA).contains(callback.getEvent());
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You've updated the application");

        postSubmitResponse.setConfirmationBody(
            "#### What happens next\r\nBoth parties have been notified and the service will be updated.\r\n\r\nThe new details will be used on all future correspondence and documents.\r\n"
        );

        return postSubmitResponse;
    }
}
