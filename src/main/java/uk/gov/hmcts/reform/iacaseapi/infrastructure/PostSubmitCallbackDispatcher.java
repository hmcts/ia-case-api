package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.CcdEventAuthorizor;

@Component
@Slf4j
public class PostSubmitCallbackDispatcher<T extends CaseData> {

    private final List<PostSubmitCallbackHandler<T>> sortedCallbackHandlers;
    private final CcdEventAuthorizor ccdEventAuthorizor;

    public PostSubmitCallbackDispatcher(
        List<PostSubmitCallbackHandler<T>> callbackHandlers,
        CcdEventAuthorizor ccdEventAuthorizor
    ) {
        requireNonNull(callbackHandlers, "callbackHandlers must not be null");
        this.ccdEventAuthorizor = ccdEventAuthorizor;
        this.sortedCallbackHandlers = callbackHandlers.stream()
            // sorting handlers by handler class name
            .sorted(Comparator.comparing(h -> h.getClass().getSimpleName()))
            .collect(Collectors.toList());
    }

    public PostSubmitCallbackResponse handle(
        Callback<T> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        log.info("Checking user event access for case reference: {}, event: '{}'",
            callback.getCaseDetails().getId(), callback.getEvent().toString());
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
