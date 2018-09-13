package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Correspondence;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Correspondences;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class CorrespondenceUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.ADD_CORRESPONDENCE;
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

        Correspondence correspondence =
            asylumCase
                .getCorrespondence()
                .orElseThrow(() -> new IllegalStateException("correspondence not present"));

        List<IdValue<Correspondence>> allCorrespondences = new ArrayList<>();

        Correspondences correspondences =
            asylumCase
                .getCorrespondences()
                .orElse(new Correspondences());

        if (correspondences.getCorrespondences().isPresent()) {
            allCorrespondences.addAll(
                correspondences.getCorrespondences().get()
            );
        }

        allCorrespondences.add(
            new IdValue<>(
                String.valueOf(Instant.now().toEpochMilli()),
                correspondence
            )
        );

        correspondences.setCorrespondences(allCorrespondences);

        asylumCase.setCorrespondences(correspondences);

        return preSubmitResponse;
    }
}
