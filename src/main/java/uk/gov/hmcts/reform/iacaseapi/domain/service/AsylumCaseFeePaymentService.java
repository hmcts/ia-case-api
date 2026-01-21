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
    private final String aboutToStartPath;

    public AsylumCaseFeePaymentService(
        AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator,
        @Value("${documentsApi.endpoint}") String feePaymentApiEndpoint,
        @Value("${documentsApi.aboutToSubmitPath}") String aboutToSubmitPath,
        @Value("${documentsApi.aboutToStartPath}") String aboutToStartPath
    ) {
        this.asylumCaseCallbackApiDelegator = asylumCaseCallbackApiDelegator;
        this.feePaymentApiEndpoint = feePaymentApiEndpoint;
        this.aboutToSubmitPath = aboutToSubmitPath;
        this.aboutToStartPath = aboutToStartPath;
    }

    public AsylumCase aboutToStart(
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return asylumCaseCallbackApiDelegator.delegate(
                callback,
                feePaymentApiEndpoint + aboutToStartPath
        );
    }

    public AsylumCase aboutToSubmit(
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return asylumCaseCallbackApiDelegator.delegate(
                callback,
                feePaymentApiEndpoint + aboutToSubmitPath
        );
    }

}
