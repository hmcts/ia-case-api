package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.FAILED;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class AppealPaymentConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.PAYMENT_APPEAL;
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

        final String paymentAccountNumber = asylumCase
            .read(PBA_NUMBER, String.class)
            .orElse("");

        final BigDecimal fee = asylumCase
            .read(FEE_AMOUNT, BigDecimal.class)
            .orElseThrow(() -> new IllegalStateException("Fee amount is not present"));

        final Optional<PaymentStatus> paymentStatus = asylumCase
            .read(PAYMENT_STATUS, PaymentStatus.class);

        if (paymentStatus.equals(Optional.of(FAILED))) {

            String paymentErrorMessage =
                requireNonNull(callback.getCaseDetails().getCaseData().read(PAYMENT_ERROR_MESSAGE, String.class)
                    .<RequiredFieldMissingException>orElseThrow(() -> new RequiredFieldMissingException("payment error message missing")));

            postSubmitResponse.setConfirmationHeader("");
            postSubmitResponse.setConfirmationBody(
                "![Payment failed confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/paymentFailed.png)\n"
                + "#### Do this next\n\n"
                + "You still need to [pay for your appeal](/case/IA/Asylum/"
                + callback.getCaseDetails().getId() + "/trigger/paymentAppeal)."
                + "\n\n\n#### Payment failed"
                + "\n\n#### Payment reference number\n"
                + paymentReferenceNumber
                + "\n\n#### Payment by account number\n"
                + paymentAccountNumber
                + "\n\n#### Fee\n"
                + "£" + fee
                + "\n\n#### Reason for failed payment\n"
                + paymentErrorMessage
            );
        } else {
            postSubmitResponse.setConfirmationHeader("# You have paid for the appeal \n# You still need to submit it");
            postSubmitResponse.setConfirmationBody(
                "#### Do this next\n\n"
                + "You still need to [submit your appeal](/case/IA/Asylum/"
                + callback.getCaseDetails().getId() + "/trigger/submitAppeal)"
                + "\n\n\n#### Payment successful"
                + "\n\n#### Payment reference number\n"
                + paymentReferenceNumber
                + "\n\n#### Payment by account number\n"
                + paymentAccountNumber
                + "\n\n#### Fee\n"
                + "£" + fee
            );
        }

        return postSubmitResponse;
    }
}
