package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPreSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FirstUnapprovedTimeExtensionExtractor;

@Component
public class TimeExtensionReviewPreparer implements CcdEventPreSubmitHandler<AsylumCase> {

    private static final org.slf4j.Logger LOG = getLogger(TimeExtensionReviewPreparer.class);

    private final FirstUnapprovedTimeExtensionExtractor firstUnapprovedTimeExtensionExtractor;

    public TimeExtensionReviewPreparer(
        @Autowired FirstUnapprovedTimeExtensionExtractor firstUnapprovedTimeExtensionExtractor
    ) {
        this.firstUnapprovedTimeExtensionExtractor = firstUnapprovedTimeExtensionExtractor;
    }

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_START
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

        asylumCase.clearTimeExtensionUnderReview();
        asylumCase.clearTimeExtensionReview();

        CcdEventPreSubmitResponse<AsylumCase> preSubmitResponse =
            new CcdEventPreSubmitResponse<>(asylumCase);

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
