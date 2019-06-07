package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.CcdEventAuthorizor;

@Component
public class PreSubmitCallbackDispatcher<T extends CaseData> {

    private final CcdEventAuthorizor ccdEventAuthorizor;
    private final List<PreSubmitCallbackHandler<T>> sortedCallbackHandlers;

    public PreSubmitCallbackDispatcher(
        CcdEventAuthorizor ccdEventAuthorizor,
        List<PreSubmitCallbackHandler<T>> callbackHandlers
    ) {
        requireNonNull(ccdEventAuthorizor, "ccdEventAuthorizor must not be null");
        requireNonNull(callbackHandlers, "callbackHandlers must not be null");

        // sorting handlers by dispatch priority and handler name
        Comparator<PreSubmitCallbackHandler<T>> priorityAndHandlerNameComparator = Comparator
            .comparingInt((ToIntFunction<PreSubmitCallbackHandler<T>>) h -> h.getDispatchPriority().ordinal())
            .thenComparing(h -> h.getClass().getSimpleName());

        this.ccdEventAuthorizor = ccdEventAuthorizor;
        this.sortedCallbackHandlers = callbackHandlers.stream()
            .sorted(priorityAndHandlerNameComparator)
            .collect(Collectors.toList());
    }

    public PreSubmitCallbackResponse<T> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        ccdEventAuthorizor.throwIfNotAuthorized(callback.getEvent());

        T caseData =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<T> callbackResponse =
            new PreSubmitCallbackResponse<>(caseData);

        dispatchToHandlers(callbackStage, callback, sortedCallbackHandlers, callbackResponse);

        return callbackResponse;
    }

    private void dispatchToHandlers(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback,
        List<PreSubmitCallbackHandler<T>> callbackHandlers,
        PreSubmitCallbackResponse<T> callbackResponse
    ) {
        for (PreSubmitCallbackHandler<T> callbackHandler : callbackHandlers) {

            Callback<T> callbackForHandler = new Callback<>(
                new CaseDetails<>(
                    callback.getCaseDetails().getId(),
                    callback.getCaseDetails().getJurisdiction(),
                    callback.getCaseDetails().getState(),
                    callbackResponse.getData(),
                    callback.getCaseDetails().getCreatedDate()
                ),
                callback.getCaseDetailsBefore(),
                callback.getEvent()
            );

            if (callbackHandler.canHandle(callbackStage, callbackForHandler)) {

                PreSubmitCallbackResponse<T> callbackResponseFromHandler =
                    callbackHandler.handle(callbackStage, callbackForHandler);

                callbackResponse.setData(callbackResponseFromHandler.getData());

                if (!callbackResponseFromHandler.getErrors().isEmpty()) {
                    callbackResponse.addErrors(callbackResponseFromHandler.getErrors());
                }
            }
        }
    }
}
