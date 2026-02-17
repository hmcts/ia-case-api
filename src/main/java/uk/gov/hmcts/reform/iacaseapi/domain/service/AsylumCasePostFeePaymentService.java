package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseCallbackApiDelegator;

@Service
public class AsylumCasePostFeePaymentService implements PostFeePayment<AsylumCase> {

    private final AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;
    private final String feePaymentApiEndpoint;
    private final String ccdSubmittedPath;

    public AsylumCasePostFeePaymentService(
        AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator,
        @Value("${documentsApi.endpoint}") String feePaymentApiEndpoint,
        @Value("${documentsApi.ccdSubmittedPath}") String ccdSubmittedPath
    ) {
        this.asylumCaseCallbackApiDelegator = asylumCaseCallbackApiDelegator;
        this.feePaymentApiEndpoint = feePaymentApiEndpoint;
        this.ccdSubmittedPath = ccdSubmittedPath;
    }

    public PostSubmitCallbackResponse ccdSubmitted(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return asylumCaseCallbackApiDelegator.delegatePostSubmit(
            callback,
            feePaymentApiEndpoint + ccdSubmittedPath
        );
    }

}
