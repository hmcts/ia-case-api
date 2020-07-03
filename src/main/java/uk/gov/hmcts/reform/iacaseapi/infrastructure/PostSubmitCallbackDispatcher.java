package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation.MessageBroker;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation.SendToWorkAllocation;

@Component
public class PostSubmitCallbackDispatcher<T extends CaseData> {

    private final List<PostSubmitCallbackHandler<T>> sortedCallbackHandlers;
    private final SendToWorkAllocation<T> sendToWorkAllocation;
    private final MessageBroker<T> messageBroker;

    public PostSubmitCallbackDispatcher(
            List<PostSubmitCallbackHandler<T>> callbackHandlers,
            SendToWorkAllocation<T> sendToWorkAllocation,
            MessageBroker<T> messageBroker) {
        this.sendToWorkAllocation = sendToWorkAllocation;
        this.messageBroker = messageBroker;
        requireNonNull(callbackHandlers, "callbackHandlers must not be null");
        this.sortedCallbackHandlers = callbackHandlers.stream()
            // sorting handlers by handler class name
            .sorted(Comparator.comparing(h -> h.getClass().getSimpleName()))
            .collect(Collectors.toList());
    }

    public PostSubmitCallbackResponse handle(
        Callback<T> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        PostSubmitCallbackResponse callbackResponse =
            new PostSubmitCallbackResponse();

        for (PostSubmitCallbackHandler<T> callbackHandler : sortedCallbackHandlers) {

            if (callbackHandler.canHandle(callback)) {

                PostSubmitCallbackResponse callbackResponseFromHandler =
                    callbackHandler.handle(callback);

                callbackResponseFromHandler
                    .getConfirmationHeader()
                    .ifPresent(callbackResponse::setConfirmationHeader);

                callbackResponseFromHandler
                    .getConfirmationBody()
                    .ifPresent(callbackResponse::setConfirmationBody);

                break;
            }
        }

        sendToWorkAllocation.handle(callback);
//        messageBroker.sendToCamunda(callback);

        return callbackResponse;
    }
}
