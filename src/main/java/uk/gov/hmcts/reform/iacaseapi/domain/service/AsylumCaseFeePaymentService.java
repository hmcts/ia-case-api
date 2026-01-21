package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CallbackApiDelegator;

@Service
public class AsylumCaseFeePaymentService implements FeePayment<AsylumCase> {

    private final CallbackApiDelegator callbackApiDelegator;
    private final String feePaymentApiEndpoint;
    private final String aboutToSubmitPath;
    private final String aboutToStartPath;

    public AsylumCaseFeePaymentService(
        CallbackApiDelegator callbackApiDelegator,
        @Value("${paymentApi.endpoint}") String feePaymentApiEndpoint,
        @Value("${paymentApi.aboutToSubmitPath}") String aboutToSubmitPath,
        @Value("${paymentApi.aboutToStartPath}") String aboutToStartPath
    ) {
        this.callbackApiDelegator = callbackApiDelegator;
        this.feePaymentApiEndpoint = feePaymentApiEndpoint;
        this.aboutToSubmitPath = aboutToSubmitPath;
        this.aboutToStartPath = aboutToStartPath;
    }

    public AsylumCase aboutToStart(
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callbackApiDelegator.delegate(
                callback,
                feePaymentApiEndpoint + aboutToStartPath
        );
    }

    public AsylumCase aboutToSubmit(
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callbackApiDelegator.delegate(
                callback,
                feePaymentApiEndpoint + aboutToSubmitPath
        );
    }

}
