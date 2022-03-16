package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class BailApplicationSavedConfirmation implements PostSubmitCallbackHandler<BailCase> {

    @Override
    public boolean canHandle(Callback<BailCase> callback) {
        return (callback.getEvent() == Event.START_APPLICATION);
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<BailCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationBody(
            "### Do this next\n\n"
                + "Review and edit the application if necessary. Submit the application when you’re ready."
        );

        postSubmitResponse.setConfirmationHeader("# You have saved this application");

        return postSubmitResponse;
    }
}
