package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;

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

        dispatchToHandlers(
            stage,
            ccdEvent,
            preSubmitHandlers
                .stream()
                .filter(preSubmitHandler -> preSubmitHandler.getDispatchPriority() == DispatchPriority.EARLY)
                .collect(Collectors.toList()),
            preSubmitResponse
        );

        dispatchToHandlers(
            stage,
            ccdEvent,
            preSubmitHandlers
                .stream()
                .filter(preSubmitHandler -> preSubmitHandler.getDispatchPriority() == DispatchPriority.LATE)
                .collect(Collectors.toList()),
            preSubmitResponse
        );

        return preSubmitResponse;
    }

    private void dispatchToHandlers(
        Stage stage,
        CcdEvent<T> ccdEvent,
        List<CcdEventPreSubmitHandler<T>> preSubmitHandlers,
        CcdEventPreSubmitResponse<T> preSubmitResponse
    ) {
        for (CcdEventPreSubmitHandler<T> preSubmitHandler : preSubmitHandlers) {

            if (preSubmitHandler.canHandle(stage, ccdEvent)) {

                CcdEventPreSubmitResponse<T> preSubmitResponseFromHandler =
                    preSubmitHandler.handle(stage, ccdEvent);

                preSubmitResponse.getErrors().addAll(preSubmitResponseFromHandler.getErrors());
                preSubmitResponse.getWarnings().addAll(preSubmitResponseFromHandler.getWarnings());
            }
        }
    }
}
