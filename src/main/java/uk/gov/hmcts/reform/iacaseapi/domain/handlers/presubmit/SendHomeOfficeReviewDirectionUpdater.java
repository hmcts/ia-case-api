package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPreSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;

@Component
public class SendHomeOfficeReviewDirectionUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    private final DirectionAppender directionAppender;

    public SendHomeOfficeReviewDirectionUpdater(
        @Autowired DirectionAppender directionAppender
    ) {
        this.directionAppender = directionAppender;
    }

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

        directionAppender.append(asylumCase, directionToSend);
        
        asylumCase.clearHomeOfficeReviewDirection();

        return preSubmitResponse;
    }
}
