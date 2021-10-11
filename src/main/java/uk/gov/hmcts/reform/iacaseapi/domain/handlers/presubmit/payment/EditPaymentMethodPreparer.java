package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.*;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
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

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse
            = new PreSubmitCallbackResponse<>(asylumCase);

        final AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
                .orElseThrow(() -> new IllegalStateException("Appeal type is not present"));

        final PaymentStatus paymentStatus = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)
                .orElse(PAYMENT_PENDING);

        if (paymentStatus == PAID) {
            callbackResponse.addError("You cannot change the payment method because you have already "
                    + "paid the fee for this appeal.");
            return callbackResponse;
        }

        String paymentOption;
        switch (appealType) {
            case EA:
            case HU:
                paymentOption = asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                        .orElseThrow(() -> new IllegalStateException("Payment option is not present"));
                if (!Arrays.asList("payNow").contains(paymentOption)) {
                    callbackResponse.addError("You cannot change the payment method to card because you have "
                            + "already selected to pay by card.");
                }

                break;

            case PA:
                paymentOption = asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                        .orElseThrow(() -> new IllegalStateException("Payment option is not present"));
                if (!Arrays.asList("payNow", "payLater").contains(paymentOption)) {
                    callbackResponse.addError("You cannot change the payment method to card because you have "
                            + "already selected to pay by card.");
                }
                break;

            case DC:
            case RP:
                callbackResponse.addError("You cannot change the payment method because there is no "
                        + "fee for this appeal.");
                break;

            default:
                break;
        }

        return callbackResponse;
    }
}
