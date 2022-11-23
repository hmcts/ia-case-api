package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAID_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.SuperAppealType.EA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.SuperAppealType.HU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.SuperAppealType.PA;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SuperAppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;

@Component
public class MarkPaymentPaidStateHandler implements PreSubmitCallbackStateHandler<AsylumCase> {

    private final boolean isfeePaymentEnabled;

    public MarkPaymentPaidStateHandler(
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
               && callback.getEvent() == Event.MARK_APPEAL_PAID
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

        asylumCase.write(PAYMENT_STATUS, PaymentStatus.PAID);

        String paymentDate = asylumCase.read(PAID_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException("Paid date is not present"));
        asylumCase.write(PAYMENT_DATE,
            LocalDate.parse(paymentDate).format(DateTimeFormatter.ofPattern("d MMM yyyy")));

        SuperAppealType superAppealType = SuperAppealType.mapFromAsylumCaseAppealType(asylumCase)
            .orElseThrow(() -> new IllegalStateException("AppealType or SuperAppealType not present"));

        if (superAppealType == EA || superAppealType == HU) {
            return new PreSubmitCallbackResponse<>(asylumCase, State.APPEAL_SUBMITTED);
        }

        if (superAppealType == PA && State.PENDING_PAYMENT == currentState) {

            return new PreSubmitCallbackResponse<>(asylumCase, State.APPEAL_SUBMITTED);
        }

        return new PreSubmitCallbackResponse<>(asylumCase, currentState);
    }
}
