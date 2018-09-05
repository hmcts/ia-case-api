package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPostSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPostSubmitHandler;

@Component
public class ServeDirectionConfirmation implements CcdEventPostSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.SUBMITTED
               && ccdEvent.getEventId() == EventId.SERVE_DIRECTION;
    }

    public CcdEventPostSubmitResponse handle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        if (!canHandle(stage, ccdEvent)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            ccdEvent
                .getCaseDetails()
                .getCaseData();

        CcdEventPostSubmitResponse postSubmitResponse =
            new CcdEventPostSubmitResponse();

        Direction directionToServe =
            asylumCase
                .getDirection()
                .orElseThrow(() -> new IllegalStateException("direction not present"));

        String serveDeadlineUrl =
            "/case/SSCS/Asylum/" + ccdEvent.getCaseDetails().getId() + "/trigger/serveDirection";

        if (directionToServe
            .getDirection()
            .orElse("")
            .toLowerCase()
            .contains("deadline")) {

            postSubmitResponse.setConfirmationHeader("# You have served a deadline direction");
            postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                + "Now that you have served the deadline direction you can go on to "
                + "[" + serveDeadlineUrl + "](" + serveDeadlineUrl + " \"serve another direction\") "
                + "or click below."
            );

        } else {

            postSubmitResponse.setConfirmationHeader("# You have served a direction");
            postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                + "Now that you have served a direction you can go on to "
                + "[" + serveDeadlineUrl + "](" + serveDeadlineUrl + " \"serve another direction\") "
                + "or click below."
            );
        }

        return postSubmitResponse;
    }
}
