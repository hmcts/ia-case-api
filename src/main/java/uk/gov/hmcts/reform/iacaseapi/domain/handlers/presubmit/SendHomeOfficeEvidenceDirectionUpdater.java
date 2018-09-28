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
public class SendHomeOfficeEvidenceDirectionUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    private final DirectionAppender directionAppender;

    public SendHomeOfficeEvidenceDirectionUpdater(
        @Autowired DirectionAppender directionAppender
    ) {
        this.directionAppender = directionAppender;
    }

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.SEND_HOME_OFFICE_EVIDENCE_DIRECTION;
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

        Direction homeOfficeEvidenceDirection =
            asylumCase
                .getHomeOfficeEvidenceDirection()
                .orElseThrow(() -> new IllegalStateException("homeOfficeEvidenceDirection not present"));

        Direction directionToSend =
            new Direction(
                "homeOfficeEvidence",
                homeOfficeEvidenceDirection
                    .getDescription()
                    .orElseThrow(() -> new IllegalStateException("homeOfficeEvidenceDirection description not present")),
                "respondent",
                homeOfficeEvidenceDirection
                    .getDueDate()
                    .orElseThrow(() -> new IllegalStateException("homeOfficeEvidenceDirection dueDate not present"))
            );

        directionAppender.append(asylumCase, directionToSend);

        asylumCase.clearHomeOfficeEvidenceDirection();

        return preSubmitResponse;
    }
}
