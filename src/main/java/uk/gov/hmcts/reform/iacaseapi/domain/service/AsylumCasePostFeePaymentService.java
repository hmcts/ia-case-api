package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CallbackApiDelegator;

@Service
public class AsylumCasePostFeePaymentService implements PostFeePayment<AsylumCase> {

    private final CallbackApiDelegator callbackApiDelegator;
    private final String feePaymentApiEndpoint;
    private final String ccdSubmittedPath;

    public AsylumCasePostFeePaymentService(
        CallbackApiDelegator callbackApiDelegator,
        @Value("${paymentApi.endpoint}") String feePaymentApiEndpoint,
        @Value("${paymentApi.ccdSubmittedPath}") String ccdSubmittedPath
    ) {
        this.callbackApiDelegator = callbackApiDelegator;
        this.feePaymentApiEndpoint = feePaymentApiEndpoint;
        this.ccdSubmittedPath = ccdSubmittedPath;
    }

    public PostSubmitCallbackResponse ccdSubmitted(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callbackApiDelegator.delegatePostSubmit(
            callback,
            feePaymentApiEndpoint + ccdSubmittedPath
        );
    }

}
