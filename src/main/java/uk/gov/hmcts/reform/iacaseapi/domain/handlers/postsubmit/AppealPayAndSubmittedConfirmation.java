package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class AppealPayAndSubmittedConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final AppealPaymentConfirmationProvider appealPaymentConfirmationProvider;

    public AppealPayAndSubmittedConfirmation(AppealPaymentConfirmationProvider appealPaymentConfirmationProvider) {
        this.appealPaymentConfirmationProvider = appealPaymentConfirmationProvider;
    }

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
        final String paymentReferenceLabel = "\n\n#### Payment reference number\n";
        final String accountNumberLabel = "\n\n#### Payment by Account number\n";
        final String feeLabel = "\n\n#### Fee\n";

        YesOrNo submissionOutOfTime =
            requireNonNull(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)
                .<RequiredFieldMissingException>orElseThrow(() -> new RequiredFieldMissingException("submission out of time is a required field")));

        if (appealPaymentConfirmationProvider.getPaymentStatus(asylumCase).equals(Optional.of(FAILED))) {

            String paymentErrorMessage =
                requireNonNull(callback.getCaseDetails().getCaseData().read(PAYMENT_ERROR_MESSAGE, String.class)
                    .<RequiredFieldMissingException>orElseThrow(() -> new RequiredFieldMissingException("payment error message missing")));

            postSubmitResponse.setConfirmationHeader("");
            postSubmitResponse.setConfirmationBody(
                "![Payment failed confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/paymentFailed.png)\n"
                + "#### Do this next\n\n"
                + "Call 01633 652 125 (option 3) or email MiddleOffice.DDServices@liberata.com to try to resolve the payment issue.\n\n"
                + "If you need to submit the appeal urgently, you can [edit your appeal](/case/IA/Asylum/"
                + callback.getCaseDetails().getId() + "/trigger/editAppeal) and change the payment method."
                + "\n\n\n#### Payment failed"
                + paymentReferenceLabel
                + appealPaymentConfirmationProvider.getPaymentReferenceNumber(asylumCase)
                + accountNumberLabel
                + appealPaymentConfirmationProvider.getPaymentAccountNumber(asylumCase)
                + feeLabel
                + "£" + appealPaymentConfirmationProvider.getFee(asylumCase)
                + "\n\n#### Reason for failed payment\n"
                + paymentErrorMessage
            );
        } else {
            if (submissionOutOfTime.equals(NO)) {
                postSubmitResponse.setConfirmationHeader("# Your appeal has been paid for and submitted");
                postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                    + "You will receive an email confirming that this appeal has been submitted successfully."
                    + "\n\n\n#### Payment successful"
                    + paymentReferenceLabel
                    + appealPaymentConfirmationProvider.getPaymentReferenceNumber(asylumCase)
                    + accountNumberLabel
                    + appealPaymentConfirmationProvider.getPaymentAccountNumber(asylumCase)
                    + feeLabel
                    + "£" + appealPaymentConfirmationProvider.getFee(asylumCase)
                );
            } else {
                postSubmitResponse.setConfirmationHeader("");
                postSubmitResponse.setConfirmationBody(
                    "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimePaidConfirmation.png)\n"
                    + "## What happens next\n\n"
                    + "A Tribunal Caseworker will review the reasons your appeal was out of time and you will be notified if it can proceed."
                    + "\n\n\n#### Payment successful"
                    + paymentReferenceLabel
                    + appealPaymentConfirmationProvider.getPaymentReferenceNumber(asylumCase)
                    + accountNumberLabel
                    + appealPaymentConfirmationProvider.getPaymentAccountNumber(asylumCase)
                    + feeLabel
                    + "£" + appealPaymentConfirmationProvider.getFee(asylumCase)
                );
            }
        }

        return postSubmitResponse;
    }
}
