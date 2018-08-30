package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPostSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPostSubmitHandler;

@Component
public class DraftAppealStartedConfirmation implements CcdEventPostSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.SUBMITTED
               && ccdEvent.getEventId() == EventId.START_DRAFT_APPEAL;
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

        String completeAppealUrl =
            "/case/SSCS/Asylum/" + ccdEvent.getCaseDetails().getId() + "/trigger/completeDraftAppeal";

        postSubmitResponse.setConfirmationHeader("# Draft appeal started");
        postSubmitResponse.setConfirmationBody(
            "#### Completing the appeal now\n\n"
            + "Your appeal is now started and you can [complete the draft appeal](" + completeAppealUrl + ") now or later.\n\n"
            + "#### Completing the appeal later\n\n"
            + "To complete the appeal later, select *'Complete draft appeal'* from the top right menu labelled *'Next step'*, on the case details page."
        );

        return postSubmitResponse;
    }
}
