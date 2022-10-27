package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AsylumCasePostFeePaymentService;

@Component
public class GenerateServiceRequestMakeAPaymentConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final AsylumCasePostFeePaymentService asylumCasePostFeePaymentService;

    public GenerateServiceRequestMakeAPaymentConfirmation(AsylumCasePostFeePaymentService asylumCasePostFeePaymentService) {
        this.asylumCasePostFeePaymentService = asylumCasePostFeePaymentService;
    }

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.GENERATE_SERVICE_REQUEST;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        Callback<AsylumCase> callbackForPaymentApi = new Callback<>(
            callback.getCaseDetails(),
            callback.getCaseDetailsBefore(),
            Event.GENERATE_SERVICE_REQUEST
        );
        asylumCasePostFeePaymentService.ccdSubmitted(callbackForPaymentApi);

        return postSubmitResponse;
    }

}
