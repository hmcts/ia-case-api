package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.slf4j.LoggerFactory.getLogger;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionReview;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class TimeExtensionReviewPreparer implements CcdEventPreSubmitHandler<AsylumCase> {

    private static final org.slf4j.Logger LOG = getLogger(TimeExtensionReviewPreparer.class);

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

        if (!asylumCase.getTimeExtensions().isPresent()
            || !asylumCase.getTimeExtensions().get().getTimeExtensions().isPresent()) {
            preSubmitResponse
                .getErrors()
                .add("There are no Time Extension requests to review");

            return preSubmitResponse;
        }

        // @see TimeExtensionReviewUpdater
        // @todo for private beta, keep the ID of the time extension request in the
        //       review so it can be properly applied back to the appropriate time
        //       extension when the even it submitted (for prototyping, we are just
        //       using the most recent extension, which may not work if a new request
        //       arrives whilst reviewing)

        List<TimeExtension> timeExtensions =
            asylumCase
                .getTimeExtensions()
                .orElseThrow(() -> new IllegalStateException("timeExtensions not present"))
                .getTimeExtensions()
                .orElseThrow(() -> new IllegalStateException("timeExtensions value not present"))
                .stream()
                .map(IdValue::getValue)
                .filter(timeExtension -> timeExtension.getStatus().orElse("").equals("submitted"))
                .collect(Collectors.toList());

        if (timeExtensions.isEmpty()) {
            preSubmitResponse
                .getErrors()
                .add("There are no submitted Time Extension requests to review");

            return preSubmitResponse;
        }

        TimeExtension timeExtensionUnderReview = timeExtensions.get(0);
        asylumCase.setTimeExtensionUnderReview(timeExtensionUnderReview);
        asylumCase.setTimeExtensionReview(null);

        LocalDate dueDate =
            LocalDate
                .now()
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
