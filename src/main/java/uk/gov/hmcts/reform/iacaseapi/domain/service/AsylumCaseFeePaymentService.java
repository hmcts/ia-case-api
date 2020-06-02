package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseCallbackApiDelegator;

@Service
public class AsylumCaseFeePaymentService implements FeePayment<AsylumCase> {

    private final AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;
    private final String feePaymentApiEndpoint;
    private final String aboutToSubmitPath;

    public AsylumCaseFeePaymentService(
        AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator,
        @Value("${paymentApi.endpoint}") String feePaymentApiEndpoint,
        @Value("${paymentApi.aboutToSubmitPath}") String aboutToSubmitPath
    ) {
        this.asylumCaseCallbackApiDelegator = asylumCaseCallbackApiDelegator;
        this.feePaymentApiEndpoint = feePaymentApiEndpoint;
        this.aboutToSubmitPath = aboutToSubmitPath;
    }

    public AsylumCase send(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return asylumCaseCallbackApiDelegator.delegate(
            callback,
                feePaymentApiEndpoint + aboutToSubmitPath
        );
    }
}
