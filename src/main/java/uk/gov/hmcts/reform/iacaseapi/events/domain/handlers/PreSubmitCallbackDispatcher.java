package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PreSubmitCallbackResponse;

@Component
public class PreSubmitCallbackDispatcher<T extends CaseData> {

    private final List<PreSubmitCallbackHandler<T>> preSubmitHandlers;

    public PreSubmitCallbackDispatcher(
        @Autowired List<PreSubmitCallbackHandler<T>> preSubmitHandlers
    ) {
        this.preSubmitHandlers = preSubmitHandlers;
    }

    public PreSubmitCallbackResponse<T> handle(
        CallbackStage callbackStage,
        Callback<T> callback
    ) {
        if (callbackStage != CallbackStage.ABOUT_TO_START
            && callbackStage != CallbackStage.ABOUT_TO_SUBMIT) {
            throw new IllegalArgumentException("callbackStage is not pre submit");
        }

        T caseData =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<T> preSubmitResponse =
            new PreSubmitCallbackResponse<>(caseData);

        dispatchToHandlers(
            callbackStage,
            callback,
            preSubmitHandlers
                .stream()
                .filter(preSubmitHandler -> preSubmitHandler.getDispatchPriority() == DispatchPriority.EARLY)
                .collect(Collectors.toList()),
            preSubmitResponse
        );

        dispatchToHandlers(
            callbackStage,
            callback,
            preSubmitHandlers
                .stream()
                .filter(preSubmitHandler -> preSubmitHandler.getDispatchPriority() == DispatchPriority.LATE)
                .collect(Collectors.toList()),
            preSubmitResponse
        );

        return preSubmitResponse;
    }

    private void dispatchToHandlers(
        CallbackStage callbackStage,
        Callback<T> callback,
        List<PreSubmitCallbackHandler<T>> preSubmitHandlers,
        PreSubmitCallbackResponse<T> preSubmitResponse
    ) {
        for (PreSubmitCallbackHandler<T> preSubmitHandler : preSubmitHandlers) {

            if (preSubmitHandler.canHandle(callbackStage, callback)) {

                PreSubmitCallbackResponse<T> preSubmitResponseFromHandler =
                    preSubmitHandler.handle(callbackStage, callback);

                preSubmitResponse.getErrors().addAll(preSubmitResponseFromHandler.getErrors());
                preSubmitResponse.getWarnings().addAll(preSubmitResponseFromHandler.getWarnings());
            }
        }
    }
}
