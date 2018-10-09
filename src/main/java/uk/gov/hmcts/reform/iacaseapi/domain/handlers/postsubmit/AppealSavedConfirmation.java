package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPostSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPostSubmitHandler;

@Component
public class AppealSavedConfirmation implements CcdEventPostSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.SUBMITTED
               && (ccdEvent.getEventId() == EventId.START_APPEAL
                   || ccdEvent.getEventId() == EventId.CHANGE_APPEAL);
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

        String changeAppealUrl =
            "/case/" + ccdEvent.getCaseDetails().getJurisdiction() + "/Asylum/" + ccdEvent.getCaseDetails().getId() + "/trigger/changeAppeal";

        String submitAppealUrl =
            "/case/" + ccdEvent.getCaseDetails().getJurisdiction() + "/Asylum/" + ccdEvent.getCaseDetails().getId() + "/trigger/submitAppeal";

        postSubmitResponse.setConfirmationHeader("# Now submit your appeal");
        postSubmitResponse.setConfirmationBody(
            "Now [submit your appeal](" + submitAppealUrl + ").\n\n"
            + "#### Not ready to submit yet?\n\n"
            + "You can return to the case details to [make changes](" + changeAppealUrl + ")"
        );

        return postSubmitResponse;
    }
}
