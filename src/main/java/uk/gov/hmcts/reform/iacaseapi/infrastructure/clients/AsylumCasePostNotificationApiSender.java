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

    private final AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;
    private final String notificationsApiEndpoint;
    private final String ccdSubmittedPath;

    public AsylumCasePostNotificationApiSender(
        AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator,
        @Value("${notificationsApi.endpoint}") String notificationsApiEndpoint,
        @Value("${notificationsApi.ccdSubmittedPath}") String ccdSubmittedPath
    ) {
        this.asylumCaseCallbackApiDelegator = asylumCaseCallbackApiDelegator;
        this.notificationsApiEndpoint = notificationsApiEndpoint;
        this.ccdSubmittedPath = ccdSubmittedPath;
    }

    @Override
    public PostSubmitCallbackResponse send(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return asylumCaseCallbackApiDelegator.delegatePostSubmit(
            callback,
            notificationsApiEndpoint + ccdSubmittedPath
        );
    }
}
