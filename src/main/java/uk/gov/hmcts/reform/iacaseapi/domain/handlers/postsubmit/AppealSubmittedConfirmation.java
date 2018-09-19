package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPostSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPostSubmitHandler;

@Component
public class AppealSubmittedConfirmation implements CcdEventPostSubmitHandler<AsylumCase> {

    private final String bookmarkBaseUrl;

    public AppealSubmittedConfirmation(
        @Value("${bookmarkBaseUrl}") String bookmarkBaseUrl
    ) {
        this.bookmarkBaseUrl = bookmarkBaseUrl;
    }

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.SUBMITTED
               && ccdEvent.getEventId() == EventId.SUBMIT_APPEAL;
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

        String trackAppealUrl =
            bookmarkBaseUrl + "/case/SSCS/Asylum/" + ccdEvent.getCaseDetails().getId();

        postSubmitResponse.setConfirmationHeader("# Your appeal application has been submitted");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "You will receive an email confirming that this appeal has been submitted successfully.\n"
            + "You can return at any time to add more evidence to this appeal.\n\n"
            + "You will receive email updates as your case progresses. "
            + "Remember that you can track the progress of your appeal online on this link:\n"
            + "[" + trackAppealUrl + "](" + trackAppealUrl + " \"Track your appeal\")"
        );

        return postSubmitResponse;
    }
}
