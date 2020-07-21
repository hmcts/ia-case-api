package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class AppealPayAndSubmittedConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final String paymentReferenceNumber = asylumCase
                .read(PAYMENT_REFERENCE, String.class)
                .orElse("");
        String paymentAccountNumber = asylumCase
                .read(PBA_NUMBER, String.class)
                .orElse("");
        String feeWithFormat = asylumCase
                .read(FEE_AMOUNT_FOR_DISPLAY, String.class)
                .orElse("");

        postSubmitResponse.setConfirmationHeader("# Your appeal has been paid for and submitted");
        postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                        + "You will receive an email confirming that this appeal has been submitted successfully."
                        + "\n\n\n#### Payment successful"
                        + "\n\n#### Payment reference number\n"
                        + paymentReferenceNumber
                        + "\n\n#### Payment by Account number\n"
                        + paymentAccountNumber
                        + "\n\n#### Fee\n"
                        + feeWithFormat
        );

        return postSubmitResponse;
    }
}
