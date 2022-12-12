package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation.EventValid;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation.EventValidCheckers;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.CcdEventAuthorizor;

@Component
public class PreSubmitCallbackDispatcher<T extends CaseData> {

    private final CcdEventAuthorizor ccdEventAuthorizor;
    private final List<PreSubmitCallbackHandler<T>> sortedCallbackHandlers;
    private final List<PreSubmitCallbackStateHandler<T>> callbackStateHandlers;
    private final EventValidCheckers<T> eventValidChecker;

    public PreSubmitCallbackDispatcher(
            CcdEventAuthorizor ccdEventAuthorizor,
            List<PreSubmitCallbackHandler<T>> callbackHandlers,
            EventValidCheckers<T> eventValidChecker,
            List<PreSubmitCallbackStateHandler<T>> callbackStateHandlers
    ) {
        requireNonNull(ccdEventAuthorizor, "ccdEventAuthorizor must not be null");
        requireNonNull(callbackHandlers, "callbackHandlers must not be null");
        this.ccdEventAuthorizor = ccdEventAuthorizor;
        this.sortedCallbackHandlers = callbackHandlers.stream()
            // sorting handlers by handler class name
            .sorted(Comparator.comparing(h -> h.getClass().getSimpleName()))
            .collect(Collectors.toList());
        this.eventValidChecker = eventValidChecker;
        this.callbackStateHandlers = callbackStateHandlers.stream()
            // sorting handlers by handler class name
            .sorted(Comparator.comparing(h -> h.getClass().getSimpleName()))
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

        EventValid check = eventValidChecker.check(callback);

        if (check.isValid()) {

            dispatchToHandlers(callbackStage, callback, sortedCallbackHandlers, callbackResponse, DispatchPriority.EARLIEST);
            dispatchToHandlers(callbackStage, callback, sortedCallbackHandlers, callbackResponse, DispatchPriority.EARLY);

            State state = dispatchToStateHandlers(callbackStage, callback, callbackStateHandlers, callbackResponse);

            if (state != null) {
                callbackResponse = new PreSubmitCallbackResponse<>(callbackResponse.getData(), state);
                callback = new Callback<>(
                    new CaseDetails<>(
                        callback.getCaseDetails().getId(),
                        callback.getCaseDetails().getJurisdiction(),
                        state,
                        callbackResponse.getData(),
                        callback.getCaseDetails().getCreatedDate(),
                        callback.getCaseDetails().getSecurityClassification(),
                        callback.getCaseDetails().getSupplementaryData()
                    ),
                    callback.getCaseDetailsBefore(),
                    callback.getEvent()
                );
            }

            dispatchToHandlers(callbackStage, callback, sortedCallbackHandlers, callbackResponse, DispatchPriority.LATE);
            dispatchToHandlers(callbackStage, callback, sortedCallbackHandlers, callbackResponse, DispatchPriority.LATEST);

        } else {
            callbackResponse.addError(check.getInvalidReason());
        }
        return callbackResponse;
    }

    private State dispatchToStateHandlers(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback,
        List<PreSubmitCallbackStateHandler<T>> callbackStateHandlers,
        PreSubmitCallbackResponse<T> callbackResponse) {

        State finalState = null;
        for (PreSubmitCallbackStateHandler<T> callbackStateHandler : callbackStateHandlers) {

            Callback<T> callbackForHandler = new Callback<>(
                new CaseDetails<>(
                    callback.getCaseDetails().getId(),
                    callback.getCaseDetails().getJurisdiction(),
                    callback.getCaseDetails().getState(),
                    callbackResponse.getData(),
                    callback.getCaseDetails().getCreatedDate(),
                    callback.getCaseDetails().getSecurityClassification(),
                    callback.getCaseDetails().getSupplementaryData()
                ),
                callback.getCaseDetailsBefore(),
                callback.getEvent()
            );

            if (callbackStateHandler.canHandle(callbackStage, callbackForHandler)) {

                PreSubmitCallbackResponse<T> callbackResponseFromHandler =
                    callbackStateHandler.handle(callbackStage, callbackForHandler, callbackResponse);

                finalState = callbackResponseFromHandler.getState();

                if (!callbackResponseFromHandler.getErrors().isEmpty()) {
                    callbackResponse.addErrors(callbackResponseFromHandler.getErrors());
                }
            }
        }

        return finalState;

    }

    private void dispatchToHandlers(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback,
        List<PreSubmitCallbackHandler<T>> callbackHandlers,
        PreSubmitCallbackResponse<T> callbackResponse,
        DispatchPriority dispatchPriority
    ) {
        for (PreSubmitCallbackHandler<T> callbackHandler : callbackHandlers) {

            if (callbackHandler.getDispatchPriority() == dispatchPriority) {

                Callback<T> callbackForHandler = new Callback<>(
                    new CaseDetails<>(
                        callback.getCaseDetails().getId(),
                        callback.getCaseDetails().getJurisdiction(),
                        callback.getCaseDetails().getState(),
                        callbackResponse.getData(),
                        callback.getCaseDetails().getCreatedDate(),
                        callback.getCaseDetails().getSecurityClassification(),
                        callback.getCaseDetails().getSupplementaryData()
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
}
