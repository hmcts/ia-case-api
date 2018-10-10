package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.postsubmit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PostSubmitCallbackHandler;

@Component
public class AppealSubmittedConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final String bookmarkBaseUrl;

    public AppealSubmittedConfirmation(
        @Value("${bookmarkBaseUrl}") String bookmarkBaseUrl
    ) {
        this.bookmarkBaseUrl = bookmarkBaseUrl;
    }

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.SUBMITTED
               && callback.getEventId() == EventId.SUBMIT_APPEAL;
    }

    public PostSubmitCallbackResponse handle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        String trackAppealUrl =
            bookmarkBaseUrl + "/case/" + callback.getCaseDetails().getJurisdiction() + "/Asylum/" + callback.getCaseDetails().getId();

        postSubmitResponse.setConfirmationHeader("# Your appeal application has been submitted");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "You will receive an email confirming that this appeal has been submitted successfully.\n"
            + "You can return at any time to add more evidence to this appeal.\n\n"
            + "You will receive email updates as your appeal progresses. "
            + "Remember that you can track the progress of your appeal online on this link:\n"
            + "[" + trackAppealUrl + "](" + trackAppealUrl + " \"Track your appeal\")"
        );

        return postSubmitResponse;
    }
}
