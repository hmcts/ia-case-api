package uk.gov.hmcts.reform.bailcaseapi.infrastructure;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.CcdEventAuthorizor;

@Component
public class PostSubmitCallbackDispatcher<T extends CaseData> {

    private final CcdEventAuthorizor ccdEventAuthorizor;
    private final List<PostSubmitCallbackHandler<T>> sortedCallbackHandlers;

    public PostSubmitCallbackDispatcher(
        CcdEventAuthorizor ccdEventAuthorizor,
        List<PostSubmitCallbackHandler<T>> callbackHandlers
    ) {
        requireNonNull(callbackHandlers, "callbackHandlers cannot be null");
        this.ccdEventAuthorizor = ccdEventAuthorizor;
        this.sortedCallbackHandlers = callbackHandlers.stream()
            .sorted(Comparator.comparing(h -> h.getClass().getSimpleName()))
            .collect(Collectors.toList());
    }

    public PostSubmitCallbackResponse handle(
        Callback<T> callback
    ) {
        requireNonNull(callback, "callback cannot be null");
        ccdEventAuthorizor.throwIfNotAuthorized(callback.getEvent());

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
