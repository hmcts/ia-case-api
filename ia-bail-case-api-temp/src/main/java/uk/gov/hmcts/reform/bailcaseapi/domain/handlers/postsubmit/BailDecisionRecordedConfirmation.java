package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class BailDecisionRecordedConfirmation implements PostSubmitCallbackHandler<BailCase> {

    @Override
    public boolean canHandle(Callback<BailCase> callback) {
        return (callback.getEvent() == Event.RECORD_THE_DECISION);
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
            + "This application has been decided. Download the decision notice from the "
            + "documents tab and distribute to anyone who needs to sign it. [Upload the "
            + "signed decision notice](/cases/case-details/"
            + callback.getCaseDetails().getId()
            + "/trigger/uploadSignedDecisionNotice/uploadSignedDecisionNoticesignedDecisionNoticeUpload) "
            + "when it is ready."
        );

        postSubmitResponse.setConfirmationHeader("# You have recorded the decision");

        return postSubmitResponse;
    }
}
