package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.NotificationSender;

@Service
public class BailCaseNotificationApiSender implements NotificationSender<BailCase> {

    private final BailCaseCallbackApiDelegator bailCaseCallbackApiDelegator;
    private final String notificationsApiEndpoint;
    private final String aboutToSubmitPath;

    public BailCaseNotificationApiSender(
        BailCaseCallbackApiDelegator bailCaseCallbackApiDelegator,
        @Value("${notificationsApi.endpoint}") String notificationsApiEndpoint,
        @Value("${notificationsApi.aboutToSubmitPath}") String aboutToSubmitPath
    ) {
        this.bailCaseCallbackApiDelegator = bailCaseCallbackApiDelegator;
        this.notificationsApiEndpoint = notificationsApiEndpoint;
        this.aboutToSubmitPath = aboutToSubmitPath;
    }

    public BailCase send(
        Callback<BailCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return bailCaseCallbackApiDelegator.delegate(
            callback,
            notificationsApiEndpoint + aboutToSubmitPath
        );
    }
}
