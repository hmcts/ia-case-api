package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class UpdateInterpreterDetailsConfirmation implements PostSubmitCallbackHandler<BailCase> {

    public boolean canHandle(Callback<BailCase> callback) {
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.UPDATE_INTERPRETER_DETAILS;
    }

    public PostSubmitCallbackResponse handle(Callback<BailCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse = new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# Interpreter details have been updated");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "Ensure the [interpreter booking status](/case/IA/Bail/"
            + callback.getCaseDetails().getId()
            + "/trigger/updateInterpreterBookingStatus) is updated."
        );

        return postSubmitResponse;
    }
}
