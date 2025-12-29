package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;

@Service
public class BailCaseNotificationApiSender implements NotificationSender<BailCase> {

    private final CallbackApiDelegator callbackApiDelegator;
    private final String notificationsApiEndpoint;
    private final String aboutToSubmitPath;

    public BailCaseNotificationApiSender(
        CallbackApiDelegator callbackApiDelegator,
        @Value("${notificationsApi.endpoint}") String notificationsApiEndpoint,
        @Value("${notificationsApi.aboutToSubmitPath}") String aboutToSubmitPath
    ) {
        this.callbackApiDelegator = callbackApiDelegator;
        this.notificationsApiEndpoint = notificationsApiEndpoint;
        this.aboutToSubmitPath = aboutToSubmitPath;
    }

    public BailCase send(
        Callback<BailCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callbackApiDelegator.delegate(
            callback,
            notificationsApiEndpoint + aboutToSubmitPath
        );
    }
}
