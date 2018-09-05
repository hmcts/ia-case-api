package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPostSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPostSubmitHandler;

@Component
public class UpdateSummaryConfirmation implements CcdEventPostSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.SUBMITTED
               && ccdEvent.getEventId() == EventId.UPDATE_SUMMARY;
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

        String caseSummaryTabUrl =
            "/case/SSCS/Asylum/" + ccdEvent.getCaseDetails().getId() + "#caseSummaryTab";

        String serveDirectionUrl =
            "/case/SSCS/Asylum/" + ccdEvent.getCaseDetails().getId() + "/trigger/serveDirection";

        postSubmitResponse.setConfirmationHeader("# You have updated the summary");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "Now that you have updated the "
            + "[case summary](" + caseSummaryTabUrl + ") "
            + "you can go on to "
            + "[submit the deadline direction](" + serveDirectionUrl + ") "
            + "or click below."
        );

        return postSubmitResponse;
    }
}
