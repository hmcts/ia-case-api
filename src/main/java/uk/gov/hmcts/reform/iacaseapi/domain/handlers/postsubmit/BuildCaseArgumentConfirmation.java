package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPostSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPostSubmitHandler;

@Component
public class BuildCaseArgumentConfirmation implements CcdEventPostSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.SUBMITTED
               && ccdEvent.getEventId() == EventId.BUILD_CASE_ARGUMENT;
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
            "/case/SSCS/Asylum/" + ccdEvent.getCaseDetails().getId() + "/trigger/completeDirection";

        postSubmitResponse.setConfirmationHeader("# Your update is saved");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "If you've finished building your case, [mark this direction as complete](" + completeDirectionUrl + "). "
            + "The case officer will then review the case before sending it off to the Home Office. "
            + "You can continue to add more evidence after the direction is complete."
        );

        return postSubmitResponse;
    }
}
