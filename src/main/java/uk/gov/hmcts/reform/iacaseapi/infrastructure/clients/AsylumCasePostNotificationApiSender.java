package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PostNotificationSender;

@Service
public class AsylumCasePostNotificationApiSender implements PostNotificationSender<AsylumCase> {

    private final CallbackApiDelegator callbackApiDelegator;
    private final String notificationsApiEndpoint;
    private final String ccdSubmittedPath;

    public AsylumCasePostNotificationApiSender(
        CallbackApiDelegator callbackApiDelegator,
        @Value("${notificationsApi.endpoint}") String notificationsApiEndpoint,
        @Value("${notificationsApi.asylum.ccdSubmittedPath}") String ccdSubmittedPath
    ) {
        this.callbackApiDelegator = callbackApiDelegator;
        this.notificationsApiEndpoint = notificationsApiEndpoint;
        this.ccdSubmittedPath = ccdSubmittedPath;
    }

    @Override
    public PostSubmitCallbackResponse send(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return callbackApiDelegator.delegatePostSubmit(
            callback,
            notificationsApiEndpoint + ccdSubmittedPath
        );
    }
}
