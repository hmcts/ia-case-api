package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PostSubmitCallbackHandler;

@Component
public class UploadDocumentConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.SUBMITTED
               && callback.getEventId() == EventId.UPLOAD_DOCUMENT;
    }

    public PostSubmitCallbackResponse handle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        String completeDirectionUrl =
            "/case/" + callback.getCaseDetails().getJurisdiction() + "/Asylum/" + callback.getCaseDetails().getId() + "/trigger/completeDirection";

        String uploadDocumentUrl =
            "/case/" + callback.getCaseDetails().getJurisdiction() + "/Asylum/" + callback.getCaseDetails().getId() + "/trigger/uploadDocument";

        postSubmitResponse.setConfirmationHeader("# You have uploaded a document to the case");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "If you have finished preparing the case, you can [mark the direction as completed](" + completeDirectionUrl + "). "
            + "You can continue to [add documents and evidence](" + uploadDocumentUrl + ") until the case is resolved."
        );

        return postSubmitResponse;
    }
}
