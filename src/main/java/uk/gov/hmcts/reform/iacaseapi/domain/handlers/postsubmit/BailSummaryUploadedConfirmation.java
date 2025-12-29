package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class BailSummaryUploadedConfirmation implements PostSubmitCallbackHandler<BailCase> {

    @Override
    public boolean canHandle(Callback<BailCase> callback) {
        return (callback.getEvent() == Event.UPLOAD_BAIL_SUMMARY);
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<BailCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationBody(
            "### What happens next\n\n"
            + "The bail summary is now available in the [documents tab](/cases/case-details/"
            + callback.getCaseDetails().getId()
            + "#Documents). The application will be decided at the hearing."
        );

        postSubmitResponse.setConfirmationHeader("# You have uploaded the Bail Summary");

        return postSubmitResponse;
    }
}
