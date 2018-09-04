package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionReview;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class TimeExtensionReviewUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    private static final org.slf4j.Logger LOG = getLogger(TimeExtensionReviewUpdater.class);

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

        TimeExtension timeExtensionToUpdate = timeExtensions.get(0);

        if (timeExtensionReview.getComment().isPresent()) {
            timeExtensionToUpdate.setComment(timeExtensionReview.getComment().get());
        }

        if (timeExtensionReview
            .getGrantOrDeny()
            .orElse("")
            .equals("grant")) {
            timeExtensionToUpdate.setStatus("granted");
        } else {
            timeExtensionToUpdate.setStatus("denied");
        }

        return preSubmitResponse;
    }
}
