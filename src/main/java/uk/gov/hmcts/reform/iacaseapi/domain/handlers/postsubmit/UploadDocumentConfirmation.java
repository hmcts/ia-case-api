package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPostSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPostSubmitHandler;

@Component
public class UploadDocumentConfirmation implements CcdEventPostSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.SUBMITTED
               && ccdEvent.getEventId() == EventId.UPLOAD_DOCUMENT;
    }

    public CcdEventPostSubmitResponse handle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        if (!canHandle(stage, ccdEvent)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        CcdEventPostSubmitResponse postSubmitResponse =
            new CcdEventPostSubmitResponse();

        String completeDirectionUrl =
            "/case/" + ccdEvent.getCaseDetails().getJurisdiction() + "/Asylum/" + ccdEvent.getCaseDetails().getId() + "/trigger/completeDirection";

        String uploadDocumentUrl =
            "/case/" + ccdEvent.getCaseDetails().getJurisdiction() + "/Asylum/" + ccdEvent.getCaseDetails().getId() + "/trigger/uploadDocument";

        postSubmitResponse.setConfirmationHeader("# You have uploaded a document to the case");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "If you have finished preparing the case, you can [mark the direction as completed](" + completeDirectionUrl + "). "
            + "You can continue to [add documents and evidence](" + uploadDocumentUrl + ") until the case is resolved."
        );

        return postSubmitResponse;
    }
}
