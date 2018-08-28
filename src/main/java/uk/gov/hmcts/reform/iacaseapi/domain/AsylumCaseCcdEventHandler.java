package uk.gov.hmcts.reform.iacaseapi.domain;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;

@Component
public class AsylumCaseCcdEventHandler  {

    private final List<CcdEventHandler<AsylumCase>> ccdEventHandlers;

    public AsylumCaseCcdEventHandler(
        @Autowired List<CcdEventHandler<AsylumCase>> ccdEventHandlers
    ) {
        this.ccdEventHandlers = ccdEventHandlers;
    }

    public CcdEventResponse<AsylumCase> handle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        AsylumCase asylumCase =
            ccdEvent
                .getCaseDetails()
                .getCaseData();

        CcdEventResponse<AsylumCase> ccdEventResponse =
            new CcdEventResponse<>(asylumCase);

        for (CcdEventHandler<AsylumCase> ccdEventHandler : ccdEventHandlers) {

            if (ccdEventHandler.canHandle(stage, ccdEvent)) {

                CcdEventResponse<AsylumCase> ccdEventResponseFromHandler =
                    ccdEventHandler.handle(stage, ccdEvent);

                ccdEventResponse.getErrors().addAll(ccdEventResponseFromHandler.getErrors());
                ccdEventResponse.getWarnings().addAll(ccdEventResponseFromHandler.getWarnings());

                ccdEventResponse.setConfirmationHeader(ccdEventResponseFromHandler.getConfirmationHeader().orElse(null));
                ccdEventResponse.setConfirmationBody(ccdEventResponseFromHandler.getConfirmationBody().orElse(null));
            }
        }

        return ccdEventResponse;
    }
}
