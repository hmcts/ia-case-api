package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.PostNotificationSender;

import static java.util.Objects.requireNonNull;

@Service
public class BailCasePostNotificationApiSender implements PostNotificationSender<BailCase> {

    private final BailCaseCallbackApiDelegator bailCaseCallbackApiDelegator;
    private final String notificationsApiEndpoint;
    private final String ccdSubmittedPath;

    public BailCasePostNotificationApiSender(
        BailCaseCallbackApiDelegator bailCaseCallbackApiDelegator,
        @Value("${notificationsApi.endpoint}") String notificationsApiEndpoint,
        @Value("${notificationsApi.ccdSubmittedPath}") String ccdSubmittedPath
    ) {
        this.bailCaseCallbackApiDelegator = bailCaseCallbackApiDelegator;
        this.notificationsApiEndpoint = notificationsApiEndpoint;
        this.ccdSubmittedPath = ccdSubmittedPath;
    }

    @Override
    public PostSubmitCallbackResponse send(Callback<BailCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return bailCaseCallbackApiDelegator.delegatePostSubmit(
            callback,
            notificationsApiEndpoint + ccdSubmittedPath
        );
    }
}
