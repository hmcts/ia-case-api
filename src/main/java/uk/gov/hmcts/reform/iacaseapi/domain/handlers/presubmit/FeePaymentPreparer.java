package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

@Component
public class FeePaymentPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeePayment<AsylumCase> feePayment;
    private final boolean isfeePaymentEnabled;

    public FeePaymentPreparer(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isfeePaymentEnabled,
        FeePayment<AsylumCase> feePayment
    ) {
        this.feePayment = feePayment;
        this.isfeePaymentEnabled = isfeePaymentEnabled;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START)
               && Arrays.asList(
                    Event.START_APPEAL,
                    Event.EDIT_APPEAL,
                    Event.PAYMENT_APPEAL,
                    Event.PAY_AND_SUBMIT_APPEAL)
                   .contains(callback.getEvent())
               && isfeePaymentEnabled;
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

        final State currentState = callback.getCaseDetails().getState();

        final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse
            = new PreSubmitCallbackResponse<>(asylumCase);

        final PaymentStatus paymentStatus = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)
            .orElse(PaymentStatus.PAYMENT_PENDING);

        asylumCase.read(APPEAL_TYPE, AppealType.class)
            .ifPresent(type -> {
                switch (type) {
                    case EA:
                    case HU:
                        asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                            .filter(option ->  option.equals("payOffline")
                                                || paymentStatus == PaymentStatus.PAID)
                            .ifPresent(s -> {
                                if (callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL) {

                                    asylumCasePreSubmitCallbackResponse
                                        .addError("The Pay and submit your appeal option is not available. "
                                                  + "Select Submit your appeal if you want to submit the appeal now.");
                                }
                                if (callback.getEvent() == Event.PAYMENT_APPEAL) {

                                    asylumCasePreSubmitCallbackResponse.addError("The Make a payment option is not available.");
                                }

                            });
                        break;

                    case PA:
                        asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                            .filter(option -> option.equals("payOffline")
                                                || paymentStatus == PaymentStatus.PAID
                                                || (option.equals("payLater") && currentState == State.APPEAL_STARTED))
                            .ifPresent(s -> {
                                if (callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL) {

                                    asylumCasePreSubmitCallbackResponse
                                        .addError("The Pay and submit your appeal option is not available. "
                                                  + "Select Submit your appeal if you want to submit the appeal now.");
                                }
                                if (callback.getEvent() == Event.PAYMENT_APPEAL) {

                                    asylumCasePreSubmitCallbackResponse.addError("The Make a payment option is not available.");
                                }
                            });
                        break;

                    default:
                        break;

                }
            });


        asylumCase.write(IS_FEE_PAYMENT_ENABLED, isfeePaymentEnabled ? YesOrNo.YES : YesOrNo.NO);

        if (asylumCasePreSubmitCallbackResponse.getErrors().isEmpty()) {
            asylumCasePreSubmitCallbackResponse.setData(feePayment.aboutToStart(callback));
        }

        return asylumCasePreSubmitCallbackResponse;
    }

}
