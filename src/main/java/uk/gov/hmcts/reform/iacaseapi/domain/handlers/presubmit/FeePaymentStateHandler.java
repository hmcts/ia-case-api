package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

@Component
@Slf4j
public class FeePaymentStateHandler implements PreSubmitCallbackStateHandler<AsylumCase> {

    private final FeePayment<AsylumCase> feePayment;
    private final boolean isfeePaymentEnabled;

    public FeePaymentStateHandler(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isfeePaymentEnabled,
        FeePayment<AsylumCase> feePayment
    ) {
        this.isfeePaymentEnabled = isfeePaymentEnabled;
        this.feePayment = feePayment;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && Arrays.asList(
            Event.PAYMENT_APPEAL,
            Event.SUBMIT_APPEAL
        ).contains(callback.getEvent())
               && isfeePaymentEnabled;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback,
        PreSubmitCallbackResponse<AsylumCase> callbackResponse
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final State currentState =
            callback
                .getCaseDetails()
                .getState();

        final AppealType appealType = asylumCase
            .read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("AppealType is not present"));

        asylumCase = (callback.getEvent() == Event.PAYMENT_APPEAL
                      && (appealType == HU || appealType == EA || appealType == PA))

            ? feePayment.aboutToSubmit(callback)
            : asylumCase;

        final PaymentStatus paymentStatus = asylumCase
            .read(PAYMENT_STATUS, PaymentStatus.class).orElse(PaymentStatus.PAYMENT_DUE);

        switch (currentState) {

            case PAYMENT_PENDING:
                return callback.getEvent() == Event.PAYMENT_APPEAL
                       && paymentStatus == PaymentStatus.PAID
                    ? new PreSubmitCallbackResponse<>(asylumCase, State.APPEAL_SUBMITTED)
                    : new PreSubmitCallbackResponse<>(asylumCase, currentState);

            default:
                return callback.getEvent() == Event.SUBMIT_APPEAL
                       && paymentStatus != PaymentStatus.PAID
                       && (appealType == HU || appealType == EA)
                    ? new PreSubmitCallbackResponse<>(asylumCase, State.PAYMENT_PENDING)
                    : new PreSubmitCallbackResponse<>(asylumCase, currentState);

        }
    }
}
