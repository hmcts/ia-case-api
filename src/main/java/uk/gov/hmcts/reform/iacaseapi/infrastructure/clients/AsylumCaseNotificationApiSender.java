package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;

@Service
public class AsylumCaseNotificationApiSender implements NotificationSender<AsylumCase> {

    private final AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;
    private final String notificationsApiEndpoint;
    private final String aboutToSubmitPath;

    public AsylumCaseNotificationApiSender(
        AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator,
        @Value("${notificationsApi.endpoint}") String notificationsApiEndpoint,
        @Value("${notificationsApi.aboutToSubmitPath}") String aboutToSubmitPath
    ) {
        this.asylumCaseCallbackApiDelegator = asylumCaseCallbackApiDelegator;
        this.notificationsApiEndpoint = notificationsApiEndpoint;
        this.aboutToSubmitPath = aboutToSubmitPath;
    }

    public AsylumCase send(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return asylumCaseCallbackApiDelegator.delegate(
            callback,
            notificationsApiEndpoint + aboutToSubmitPath
        );
    }
}
