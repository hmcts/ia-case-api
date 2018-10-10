package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PostSubmitCallbackHandler;

@Component
public class SubmitForReviewConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.SUBMITTED
               && callback.getEventId() == EventId.SUBMIT_FOR_REVIEW;
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

        postSubmitResponse.setConfirmationHeader("# You have submitted your case");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "The Case Officer will now review the case. If they consider it ready, "
            + "they will send it to the Home Office for their response."
        );

        return postSubmitResponse;
    }
}
