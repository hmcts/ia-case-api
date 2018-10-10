package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.TimeExtensionReview;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PostSubmitCallbackHandler;

@Component
public class TimeExtensionReviewConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.SUBMITTED
               && callback.getEventId() == EventId.REVIEW_TIME_EXTENSION;
    }

    public PostSubmitCallbackResponse handle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        TimeExtensionReview timeExtensionReview =
            asylumCase
                .getTimeExtensionReview()
                .orElseThrow(() -> new IllegalStateException("timeExtensionReview not present"));

        if (timeExtensionReview
            .getGrantOrDeny()
            .orElse("")
            .equals("grant")) {

            postSubmitResponse.setConfirmationHeader("# You have granted a time extension");
            postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                + "Your decision has been sent to the legal rep who requested the time extension. "
                + "You should now update any direction due dates to reflect the extension."
            );

        } else {
            postSubmitResponse.setConfirmationHeader("# You have denied a time extension");
            postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                + "Your decision has been sent to the legal rep who requested the time extension."
            );

        }

        return postSubmitResponse;
    }
}
