package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.presubmit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.TimeExtensionReview;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.events.domain.service.FirstUnapprovedTimeExtensionExtractor;

@Component
public class TimeExtensionReviewUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    private final FirstUnapprovedTimeExtensionExtractor firstUnapprovedTimeExtensionExtractor;

    public TimeExtensionReviewUpdater(
        @Autowired FirstUnapprovedTimeExtensionExtractor firstUnapprovedTimeExtensionExtractor
    ) {
        this.firstUnapprovedTimeExtensionExtractor = firstUnapprovedTimeExtensionExtractor;
    }

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.ABOUT_TO_SUBMIT
               && callback.getEventId() == EventId.REVIEW_TIME_EXTENSION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
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

        PreSubmitCallbackResponse<AsylumCase> preSubmitResponse =
            new PreSubmitCallbackResponse<>(asylumCase);

        TimeExtensionReview timeExtensionReview =
            asylumCase
                .getTimeExtensionReview()
                .orElseThrow(() -> new IllegalStateException("timeExtensionReview not present"));

        // @see TimeExtensionReviewPreparer
        // @todo for private beta, keep the ID of the time extension request in the
        //       review so it can be properly applied back to the appropriate time
        //       extension when the event is submitted (for prototyping, we are just
        //       using the most recent extension, which may not work if a new request
        //       arrives whilst reviewing)

        TimeExtension firstUnapprovedTimeExtension =
            firstUnapprovedTimeExtensionExtractor
                .extract(asylumCase)
                .orElseThrow(() -> new IllegalStateException("unapproved timeExtension not present"));

        if (timeExtensionReview.getComment().isPresent()) {
            firstUnapprovedTimeExtension.setComment(timeExtensionReview.getComment().get());
        }

        if (timeExtensionReview
            .getGrantOrDeny()
            .orElse("")
            .equals("grant")) {

            firstUnapprovedTimeExtension.setStatus("granted");
        } else {
            firstUnapprovedTimeExtension.setStatus("denied");
        }

        return preSubmitResponse;
    }
}
