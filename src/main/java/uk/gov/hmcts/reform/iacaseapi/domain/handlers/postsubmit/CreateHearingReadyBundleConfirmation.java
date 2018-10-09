package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPostSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPostSubmitHandler;

@Component
public class CreateHearingReadyBundleConfirmation implements CcdEventPostSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.SUBMITTED
               && ccdEvent.getEventId() == EventId.CREATE_HEARING_READY_BUNDLE;
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

        String documentsTabUrl =
            "/case/" + ccdEvent.getCaseDetails().getJurisdiction() + "/Asylum/" + ccdEvent.getCaseDetails().getId() + "#documentsTab";

        postSubmitResponse.setConfirmationHeader("# You have created the hearing bundle");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "You can view the bundle in the [documents tab](" + documentsTabUrl + "). "
            + "All parties have been notified that the bundle is now available."
        );

        return postSubmitResponse;
    }
}
