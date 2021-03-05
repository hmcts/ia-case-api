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

@Component
public class PostSubmitCallbackDispatcher<T extends CaseData> {

    private final List<PostSubmitCallbackHandler<T>> sortedCallbackHandlers;

    public PostSubmitCallbackDispatcher(
        List<PostSubmitCallbackHandler<T>> callbackHandlers
    ) {
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

            }
        }

        return callbackResponse;
    }
}
