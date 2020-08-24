package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.PAID;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class AppealPaymentConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final AppealPaymentConfirmationProvider appealPaymentConfirmationProvider;

    public AppealPaymentConfirmation(AppealPaymentConfirmationProvider appealPaymentConfirmationProvider) {
        this.appealPaymentConfirmationProvider = appealPaymentConfirmationProvider;
    }

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
        final String feeLabel = "\n\n#### Fee\n";
        final String paymentReferenceNumberLabel = "\n\n#### Payment reference number\n";

        if (appealPaymentConfirmationProvider.getPaymentStatus(asylumCase).equals(Optional.of(FAILED))) {

            String paymentErrorMessage =
                requireNonNull(callback.getCaseDetails().getCaseData().read(PAYMENT_ERROR_MESSAGE, String.class)
                    .<RequiredFieldMissingException>orElseThrow(() -> new RequiredFieldMissingException("payment error message missing")));

            postSubmitResponse.setConfirmationHeader("");
            postSubmitResponse.setConfirmationBody(
                "![Payment failed confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/paymentFailed.png)\n"
                + "#### Do this next\n\n"
                + "Call 01633 652 125 (option 3) or email MiddleOffice.DDServices@liberata.com to try to resolve the payment issue.\n\n"
                + "\n\n\n#### Payment failed"
                + paymentReferenceNumberLabel
                + appealPaymentConfirmationProvider.getPaymentReferenceNumber(asylumCase)
                + "\n\n#### Payment by account number\n"
                + appealPaymentConfirmationProvider.getPaymentAccountNumber(asylumCase)
                + feeLabel
                + appealPaymentConfirmationProvider.getFeeWithFormat(asylumCase)
                + "\n\n#### Reason for failed payment\n"
                + paymentErrorMessage
            );
        }

        if (appealPaymentConfirmationProvider.getPaymentStatus(asylumCase).equals(Optional.of(PAID))) {
            final State currentState =
                callback
                    .getCaseDetails()
                    .getState();

            if (currentState.equals(State.APPEAL_STARTED)) {
                postSubmitResponse.setConfirmationHeader("# You have paid for the appeal \n# You still need to submit it");
                postSubmitResponse.setConfirmationBody(
                    "#### Do this next\n\n"
                    + "You still need to [submit your appeal](/case/IA/Asylum/"
                    + callback.getCaseDetails().getId() + "/trigger/submitAppeal)"
                    + "\n\n\n#### Payment successful"
                    + paymentReferenceNumberLabel
                    + appealPaymentConfirmationProvider.getPaymentReferenceNumber(asylumCase)
                    + "\n\n#### Payment by account number\n"
                    + appealPaymentConfirmationProvider.getPaymentAccountNumber(asylumCase)
                    + feeLabel
                    + appealPaymentConfirmationProvider.getFeeWithFormat(asylumCase)
                );
            } else {
                postSubmitResponse.setConfirmationHeader("# You have paid for the appeal");
                postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                    + "You will receive a notification to confirm the payment has been made."
                    + "\n\n\n#### Payment successful"
                    + paymentReferenceNumberLabel
                    + appealPaymentConfirmationProvider.getPaymentReferenceNumber(asylumCase)
                    + "\n\n#### Payment by Account number\n"
                    + appealPaymentConfirmationProvider.getPaymentAccountNumber(asylumCase)
                    + feeLabel
                    + appealPaymentConfirmationProvider.getFeeWithFormat(asylumCase)
                );
            }
        }
        return postSubmitResponse;
    }
}
