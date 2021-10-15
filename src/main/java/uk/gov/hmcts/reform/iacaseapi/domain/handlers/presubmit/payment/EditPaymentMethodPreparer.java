package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.REJECTED;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;


@Component
public class EditPaymentMethodPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final boolean isfeePaymentEnabled;

    public EditPaymentMethodPreparer(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isfeePaymentEnabled) {
        this.isfeePaymentEnabled = isfeePaymentEnabled;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && callback.getEvent() == Event.EDIT_PAYMENT_METHOD
                && isfeePaymentEnabled);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse
            = new PreSubmitCallbackResponse<>(asylumCase);

        final PaymentStatus paymentStatus = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)
            .orElse(PaymentStatus.PAYMENT_PENDING);
        Optional<RemissionDecision> optionalRemissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);
        Optional<RemissionType> optionalRemissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
        // remission is present and remissionRejected: ChangePayment should work without failure or with failure
        // Remission is not present: user selects PBA and paymentFailed. Change payment to card should work
        // Remission is not present: user selects payLater by PBA (PA) and payment Failed. Change payment to card should work
        // Remission is present and remissionRejected: payment has been done and Change payment to card should NOT work
        if ((optionalRemissionType.isPresent() && optionalRemissionType.get() == RemissionType.NO_REMISSION && !paymentStatus.equals(PaymentStatus.FAILED))
                || (optionalRemissionType.isPresent() && optionalRemissionType.get() != RemissionType.NO_REMISSION && optionalRemissionDecision.isEmpty())
                || (optionalRemissionDecision.isPresent() && optionalRemissionDecision.get() != REJECTED && paymentStatus != PaymentStatus.FAILED)
                || (optionalRemissionDecision.isPresent() && optionalRemissionDecision.get() == REJECTED && paymentStatus == PaymentStatus.PAID)
                || (optionalRemissionType.isEmpty() && paymentStatus != PaymentStatus.FAILED) //inflight cases
        ) {
            asylumCasePreSubmitCallbackResponse.addError("You can only change the payment method to card following a failed payment using Payment by Account.");
            return asylumCasePreSubmitCallbackResponse;
        }

        return asylumCasePreSubmitCallbackResponse;
    }
}
