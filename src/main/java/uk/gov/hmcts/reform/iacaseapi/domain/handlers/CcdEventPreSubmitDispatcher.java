package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPreSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;

@Component
public class CcdEventPreSubmitDispatcher<T extends CaseData> {

    private final List<CcdEventPreSubmitHandler<T>> preSubmitHandlers;

    public CcdEventPreSubmitDispatcher(
        @Autowired List<CcdEventPreSubmitHandler<T>> preSubmitHandlers
    ) {
        this.preSubmitHandlers = preSubmitHandlers;
    }

    public CcdEventPreSubmitResponse<T> handle(
        Stage stage,
        CcdEvent<T> ccdEvent
    ) {
        T asylumCase =
            ccdEvent
                .getCaseDetails()
                .getCaseData();

        CcdEventPreSubmitResponse<T> preSubmitResponse =
            new CcdEventPreSubmitResponse<>(asylumCase);

        for (CcdEventPreSubmitHandler<T> preSubmitHandler : preSubmitHandlers) {

            if (preSubmitHandler.canHandle(stage, ccdEvent)) {

                CcdEventPreSubmitResponse<T> preSubmitResponseFromHandler =
                    preSubmitHandler.handle(stage, ccdEvent);

                preSubmitResponse.getErrors().addAll(preSubmitResponseFromHandler.getErrors());
                preSubmitResponse.getWarnings().addAll(preSubmitResponseFromHandler.getWarnings());
            }
        }

        return preSubmitResponse;
    }
}
