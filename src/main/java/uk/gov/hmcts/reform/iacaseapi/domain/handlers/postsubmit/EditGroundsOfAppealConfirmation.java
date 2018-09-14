package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPostSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPostSubmitHandler;

@Component
public class EditGroundsOfAppealConfirmation implements CcdEventPostSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.SUBMITTED
               && ccdEvent.getEventId() == EventId.EDIT_GROUNDS_OF_APPEAL;
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

        String editGroundsOfAppealUrl =
            "/case/SSCS/Asylum/" + ccdEvent.getCaseDetails().getId() + "#caseArgumentTab";

        postSubmitResponse.setConfirmationHeader("# You have edited the grounds of appeal");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "You have edited the grounds of appeal for your case. This will be shown in your case argument. "
            + "You can continue to [build your case](" + editGroundsOfAppealUrl + ") or return to case details."
        );

        return postSubmitResponse;
    }
}
