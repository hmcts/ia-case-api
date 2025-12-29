package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class EditBailApplicationConfirmation implements PostSubmitCallbackHandler<BailCase> {

    @Override
    public boolean canHandle(Callback<BailCase> callback) {
        return (callback.getEvent() == Event.EDIT_BAIL_APPLICATION);
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
            + "You still need to [submit the application](/case/IA/Bail/"
            + callback.getCaseDetails().getId()
            + "/trigger/submitApplication). If you need to make further changes you can [edit the application]"
            + "(/case/IA/Bail/"
            + callback.getCaseDetails().getId()
            + "/trigger/editBailApplication)."
        );

        postSubmitResponse.setConfirmationHeader("# Your application details have been updated");

        return postSubmitResponse;
    }

}
