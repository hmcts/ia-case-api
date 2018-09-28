package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SentDirection;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SentDirections;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class SendHomeOfficeReviewDirectionUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.SEND_HOME_OFFICE_REVIEW_DIRECTION;
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

        Direction homeOfficeReviewDirection =
            asylumCase
                .getHomeOfficeReviewDirection()
                .orElseThrow(() -> new IllegalStateException("homeOfficeReviewDirection not present"));

        Direction directionToSend =
            new Direction(
                "homeOfficeReview",
                homeOfficeReviewDirection
                    .getDescription()
                    .orElseThrow(() -> new IllegalStateException("homeOfficeReviewDirection description not present")),
                "respondent",
                homeOfficeReviewDirection
                    .getDueDate()
                    .orElseThrow(() -> new IllegalStateException("homeOfficeReviewDirection dueDate not present"))
            );

        List<IdValue<SentDirection>> allSentDirections = new ArrayList<>();

        allSentDirections.add(
            new IdValue<>(
                String.valueOf(Instant.now().toEpochMilli()),
                new SentDirection(
                    directionToSend,
                    LocalDate.now().toString(),
                    "Outstanding"
                )
            )
        );

        SentDirections sentDirections =
            asylumCase
                .getSentDirections()
                .orElse(new SentDirections());

        if (sentDirections.getSentDirections().isPresent()) {
            allSentDirections.addAll(
                sentDirections.getSentDirections().get()
            );
        }

        sentDirections.setSentDirections(allSentDirections);

        asylumCase.setSentDirections(sentDirections);

        asylumCase.clearDirection();

        return preSubmitResponse;
    }
}
