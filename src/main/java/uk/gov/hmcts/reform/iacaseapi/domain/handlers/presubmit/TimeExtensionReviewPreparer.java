package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.slf4j.LoggerFactory.getLogger;

import java.time.LocalDate;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.DeadlineDirectionDueDateExtractor;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FirstSubmittedTimeExtensionExtractor;

@Component
public class TimeExtensionReviewPreparer implements CcdEventPreSubmitHandler<AsylumCase> {

    private static final org.slf4j.Logger LOG = getLogger(TimeExtensionReviewPreparer.class);

    private final DeadlineDirectionDueDateExtractor deadlineDirectionDueDateExtractor;
    private final FirstSubmittedTimeExtensionExtractor firstSubmittedTimeExtensionExtractor;

    public TimeExtensionReviewPreparer(
        @Autowired DeadlineDirectionDueDateExtractor deadlineDirectionDueDateExtractor,
        @Autowired FirstSubmittedTimeExtensionExtractor firstSubmittedTimeExtensionExtractor
    ) {
        this.deadlineDirectionDueDateExtractor = deadlineDirectionDueDateExtractor;
        this.firstSubmittedTimeExtensionExtractor = firstSubmittedTimeExtensionExtractor;
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
        //       extension when the even it submitted (for prototyping, we are just
        //       using the most recent extension, which may not work if a new request
        //       arrives whilst reviewing)

        Optional<TimeExtension> firstSubmittedTimeExtension =
            firstSubmittedTimeExtensionExtractor
                .extract(asylumCase);

        if (!firstSubmittedTimeExtension.isPresent()) {
            preSubmitResponse
                .getErrors()
                .add("There are no submitted Time Extension requests to review");

            return preSubmitResponse;
        }

        Optional<String> deadlineDueDate =
            deadlineDirectionDueDateExtractor
                .extract(asylumCase);

        if (!deadlineDueDate.isPresent()) {
            preSubmitResponse
                .getErrors()
                .add("An evidence and summary deadline has not been served yet");

            return preSubmitResponse;
        }

        TimeExtension timeExtensionUnderReview = firstSubmittedTimeExtension.get();
        asylumCase.setTimeExtensionUnderReview(timeExtensionUnderReview);

        LocalDate dueDate =
            LocalDate
                .parse(deadlineDueDate.get())
                .plusWeeks(
                    Integer.parseInt(
                        timeExtensionUnderReview
                            .getTimeRequested()
                            .orElse("1")
                    )
                );

        TimeExtensionReview timeExtensionReview = new TimeExtensionReview();
        timeExtensionReview.setDueDate(dueDate.toString());
        asylumCase.setTimeExtensionReview(timeExtensionReview);

        return preSubmitResponse;
    }
}
