package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;

import java.util.Optional;
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

@Component
public class FeePaymentStateHandler implements PreSubmitCallbackStateHandler<AsylumCase> {

    private final boolean isfeePaymentEnabled;

    public FeePaymentStateHandler(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isfeePaymentEnabled
    ) {
        this.isfeePaymentEnabled = isfeePaymentEnabled;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL
                    || callback.getEvent() == Event.SUBMIT_APPEAL)
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

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final State currentState =
            callback
                .getCaseDetails()
                .getState();

        final Optional<PaymentStatus> paymentStatus = asylumCase
            .read(PAYMENT_STATUS, PaymentStatus.class);

        final Optional<AppealType> appealType = asylumCase.read(APPEAL_TYPE);

        State newState = getNewStateForAppeal(currentState, appealType, paymentStatus, callback.getEvent());

        return new PreSubmitCallbackResponse<>(asylumCase, newState);
    }

    protected State getNewStateForAppeal(State currentState, Optional<AppealType> appealType, Optional<PaymentStatus> paymentStatus, Event event) {

        State newState = currentState;

        if (appealType.isEmpty()) {
            return currentState;
        }

        if (paymentStatus.isEmpty()) {
            return State.APPEAL_SUBMITTED;
        }

        if (paymentStatus.get().equals(PaymentStatus.PAID)) {
            newState = State.APPEAL_SUBMITTED;
        } else if ((event == Event.SUBMIT_APPEAL)
                   && (appealType.get().equals(EA) || appealType.get().equals(HU))) {
            newState = State.PENDING_PAYMENT;
        } else if (event == Event.SUBMIT_APPEAL) {
            newState = State.APPEAL_SUBMITTED;
        }
        return newState;

    }
}
