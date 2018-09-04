package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionReview;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPreSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DeadlineDirectionExtractor;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FirstSubmittedTimeExtensionExtractor;

@Component
public class TimeExtensionReviewUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    private static final org.slf4j.Logger LOG = getLogger(TimeExtensionReviewUpdater.class);

    private final DeadlineDirectionExtractor deadlineDirectionExtractor;
    private final FirstSubmittedTimeExtensionExtractor firstSubmittedTimeExtensionExtractor;

    public TimeExtensionReviewUpdater(
        @Autowired DeadlineDirectionExtractor deadlineDirectionExtractor,
        @Autowired FirstSubmittedTimeExtensionExtractor firstSubmittedTimeExtensionExtractor
    ) {
        this.deadlineDirectionExtractor = deadlineDirectionExtractor;
        this.firstSubmittedTimeExtensionExtractor = firstSubmittedTimeExtensionExtractor;
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
        //       extension when the even it submitted (for prototyping, we are just
        //       using the most recent extension, which may not work if a new request
        //       arrives whilst reviewing)

        TimeExtension firstSubmittedTimeExtension =
            firstSubmittedTimeExtensionExtractor
                .extract(asylumCase)
                .orElseThrow(() -> new IllegalStateException("submitted timeExtension not present"));

        if (timeExtensionReview.getComment().isPresent()) {
            firstSubmittedTimeExtension.setComment(timeExtensionReview.getComment().get());
        }

        if (timeExtensionReview
            .getGrantOrDeny()
            .orElse("")
            .equals("grant")) {

            Optional<Direction> deadlineDirection =
                deadlineDirectionExtractor
                    .extract(asylumCase);

            if (deadlineDirection.isPresent()) {

                deadlineDirection
                    .get()
                    .setDueDate(
                        timeExtensionReview
                            .getDueDate()
                            .orElseThrow(() -> new IllegalStateException("dueDate not present"))
                    );
            }

            firstSubmittedTimeExtension.setStatus("granted");
        } else {
            firstSubmittedTimeExtension.setStatus("denied");
        }

        return preSubmitResponse;
    }
}
