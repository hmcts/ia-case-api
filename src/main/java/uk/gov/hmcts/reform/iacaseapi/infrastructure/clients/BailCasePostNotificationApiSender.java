package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PostNotificationSender;

import static java.util.Objects.requireNonNull;

@Service
public class BailCasePostNotificationApiSender implements PostNotificationSender<BailCase> {

    private final CallbackApiDelegator callbackApiDelegator;
    private final String notificationsApiEndpoint;
    private final String ccdSubmittedPath;

    public BailCasePostNotificationApiSender(
        CallbackApiDelegator callbackApiDelegator,
        @Value("${notificationsApi.endpoint}") String notificationsApiEndpoint,
        @Value("${notificationsApi.bail.ccdSubmittedPath}") String ccdSubmittedPath
    ) {
        this.callbackApiDelegator = callbackApiDelegator;
        this.notificationsApiEndpoint = notificationsApiEndpoint;
        this.ccdSubmittedPath = ccdSubmittedPath;
    }

    @Override
    public PostSubmitCallbackResponse send(Callback<BailCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return callbackApiDelegator.delegatePostSubmit(
            callback,
            notificationsApiEndpoint + ccdSubmittedPath
        );
    }
}
