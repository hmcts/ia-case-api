package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.time.Instant;
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
public class SendHomeOfficeEvidenceDirectionUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

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
                "sendHomeOfficeEvidence",
                homeOfficeEvidenceDirection
                    .getDescription()
                    .orElseThrow(() -> new IllegalStateException("homeOfficeEvidenceDirection description not present")),
                "respondent",
                homeOfficeEvidenceDirection
                    .getDueDate()
                    .orElseThrow(() -> new IllegalStateException("homeOfficeEvidenceDirection dueDate not present"))
            );

        List<IdValue<SentDirection>> allSentDirections = new ArrayList<>();

        SentDirections sentDirections =
            asylumCase
                .getSentDirections()
                .orElse(new SentDirections());

        if (sentDirections.getSentDirections().isPresent()) {
            allSentDirections.addAll(
                sentDirections.getSentDirections().get()
            );
        }

        allSentDirections.add(
            new IdValue<>(
                String.valueOf(Instant.now().toEpochMilli()),
                new SentDirection(
                    directionToSend,
                    "Outstanding"
                )
            )
        );

        sentDirections.setSentDirections(allSentDirections);

        asylumCase.setSentDirections(sentDirections);

        asylumCase.clearDirection();

        return preSubmitResponse;
    }
}
