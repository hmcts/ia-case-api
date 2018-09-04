package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Directions;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class ServeDirectionUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    private static final org.slf4j.Logger LOG = getLogger(ServeDirectionUpdater.class);

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.SERVE_DIRECTION;
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

        Direction directionToServe =
            asylumCase
                .getDirection()
                .orElseThrow(() -> new IllegalStateException("direction not present"));

        List<IdValue<Direction>> allDirections = new ArrayList<>();

        Directions directions =
            asylumCase
                .getDirections()
                .orElse(new Directions());

        if (directions.getDirections().isPresent()) {
            allDirections.addAll(
                directions.getDirections().get()
            );
        }

        allDirections.add(
            new IdValue<>(
                String.valueOf(Instant.now().toEpochMilli()),
                directionToServe
            )
        );

        directions.setDirections(allDirections);

        asylumCase.setDirections(directions);

        return preSubmitResponse;
    }
}
