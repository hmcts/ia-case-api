package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionReview;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPreSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FirstUnapprovedTimeExtensionExtractor;

@Component
public class TimeExtensionReviewUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    private final FirstUnapprovedTimeExtensionExtractor firstUnapprovedTimeExtensionExtractor;

    public TimeExtensionReviewUpdater(
        @Autowired FirstUnapprovedTimeExtensionExtractor firstUnapprovedTimeExtensionExtractor
    ) {
        this.firstUnapprovedTimeExtensionExtractor = firstUnapprovedTimeExtensionExtractor;
    }

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.REVIEW_TIME_EXTENSION;
    }

    public CcdEventPreSubmitResponse<AsylumCase> handle(
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

        CcdEventPreSubmitResponse<AsylumCase> preSubmitResponse =
            new CcdEventPreSubmitResponse<>(asylumCase);

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
