package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class UpdateInterpreterDetailsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.UPDATE_INTERPRETER_DETAILS;
    }

    public PostSubmitCallbackResponse handle(Callback<AsylumCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse = new PostSubmitCallbackResponse();

        String hearingsTabUrl = "/case/IA/Asylum/" + callback.getCaseDetails().getId() + "#hearing";

        postSubmitResponse.setConfirmationHeader("# Interpreter details have been updated");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
                + "You now need to update the hearing in the "
                + "[Hearings tab](" + hearingsTabUrl + ")"
                + " to ensure the new interpreter information is displayed in List Assist."
                + "\n\nIf updates need to be made to the interpreter booking status this should be completed"
                + " before updating the hearing."
        );

        return postSubmitResponse;
    }
}