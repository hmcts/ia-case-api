package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.presubmit;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.events.domain.service.FirstUnapprovedTimeExtensionExtractor;

@Component
public class TimeExtensionReviewPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final FirstUnapprovedTimeExtensionExtractor firstUnapprovedTimeExtensionExtractor;

    public TimeExtensionReviewPreparer(
        @Autowired FirstUnapprovedTimeExtensionExtractor firstUnapprovedTimeExtensionExtractor
    ) {
        this.firstUnapprovedTimeExtensionExtractor = firstUnapprovedTimeExtensionExtractor;
    }

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.ABOUT_TO_START
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

        asylumCase.clearTimeExtensionUnderReview();
        asylumCase.clearTimeExtensionReview();

        PreSubmitCallbackResponse<AsylumCase> preSubmitResponse =
            new PreSubmitCallbackResponse<>(asylumCase);

        // @see TimeExtensionReviewUpdater
        // @todo for private beta, keep the ID of the time extension request in the
        //       review so it can be properly applied back to the appropriate time
        //       extension when the event is submitted (for prototyping, we are just
        //       using the most recent extension, which may not work if a new request
        //       arrives whilst reviewing)

        Optional<TimeExtension> firstUnapprovedTimeExtension =
            firstUnapprovedTimeExtensionExtractor
                .extract(asylumCase);

        if (!firstUnapprovedTimeExtension.isPresent()) {
            preSubmitResponse
                .getErrors()
                .add("There are no Time Extension requests awaiting approval");

            return preSubmitResponse;
        }

        TimeExtension timeExtensionUnderReview = firstUnapprovedTimeExtension.get();
        asylumCase.setTimeExtensionUnderReview(timeExtensionUnderReview);

        return preSubmitResponse;
    }
}
