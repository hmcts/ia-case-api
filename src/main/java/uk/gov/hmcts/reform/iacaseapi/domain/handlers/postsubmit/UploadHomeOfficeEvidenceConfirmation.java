package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPostSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPostSubmitHandler;

@Component
public class UploadHomeOfficeEvidenceConfirmation implements CcdEventPostSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.SUBMITTED
               && ccdEvent.getEventId() == EventId.UPLOAD_HOME_OFFICE_EVIDENCE;
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

        String buildAppealDirectionUrl =
            "/case/" + ccdEvent.getCaseDetails().getJurisdiction() + "/Asylum/" + ccdEvent.getCaseDetails().getId() + "/trigger/sendBuildAppealDirection";

        postSubmitResponse.setConfirmationHeader("# You have uploaded a document to the case");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "You must now send the [build appeal direction](" + buildAppealDirectionUrl + ") to the legal representative. "
            + "This instructs the legal representative to review the Home Office evidence, and to add their own legal argument "
            + "and evidence."
        );

        return postSubmitResponse;
    }
}
