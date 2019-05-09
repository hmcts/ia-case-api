package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class AppealResponseAddedConfirmation implements PostSubmitCallbackHandler<CaseDataMap> {

    public boolean canHandle(
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.ADD_APPEAL_RESPONSE;
    }

    public PostSubmitCallbackResponse handle(
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You have uploaded the appeal response");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next?\n\n"
            + "The legal representative will now get an email directing them to review the response.\n\n"
            + "This is an automated email sent from the system - you don't need to do anything."
        );

        return postSubmitResponse;
    }
}
