package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;

@Service
public class AsylumCaseNotificationApiSender implements NotificationSender<CaseDataMap> {

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

    public CaseDataMap send(
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return asylumCaseCallbackApiDelegator.delegate(
            callback,
            notificationsApiEndpoint + aboutToSubmitPath
        );
    }
}
