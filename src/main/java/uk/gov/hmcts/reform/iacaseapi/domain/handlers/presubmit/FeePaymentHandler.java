package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EA_HU_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_OFFLINE_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.PAYMENT_DUE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

@Component
public class FeePaymentHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeePayment<AsylumCase> feePayment;
    private final boolean isfeePaymentEnabled;
    private final FeePaymentDisplayProvider feePaymentDisplayProvider;

    public FeePaymentHandler(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isfeePaymentEnabled,
        FeePayment<AsylumCase> feePayment,
        FeePaymentDisplayProvider feePaymentDisplayProvider
    ) {
        this.feePayment = feePayment;
        this.isfeePaymentEnabled = isfeePaymentEnabled;
        this.feePaymentDisplayProvider = feePaymentDisplayProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
               && (callback.getEvent() == Event.START_APPEAL
                   || callback.getEvent() == Event.EDIT_APPEAL
                   || callback.getEvent() == Event.PAYMENT_APPEAL)
               && isfeePaymentEnabled;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCaseWithPaymentStatus = feePayment.aboutToSubmit(callback);

        asylumCaseWithPaymentStatus.write(AsylumCaseFieldDefinition.IS_FEE_PAYMENT_ENABLED,
            YesOrNo.YES);

        final boolean isOfflineAppealType = asylumCaseWithPaymentStatus
            .read(APPEAL_TYPE, AppealType.class)
            .map(type -> type == HU || type == EA || type == PA).orElse(false);

        final boolean isOfflinePaymentTypeOption = asylumCaseWithPaymentStatus
            .read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)
            .map(type -> type.equals("payOffline")).orElse(false);

        final boolean isOfflinePaymentTypeOptionPA = asylumCaseWithPaymentStatus
            .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
            .map(type -> type.equals("payOffline")).orElse(false);

        if (isOfflineAppealType && (isOfflinePaymentTypeOption || isOfflinePaymentTypeOptionPA)) {
            asylumCaseWithPaymentStatus.write(PAYMENT_OFFLINE_FOR_DISPLAY, "Payment pending");
        }

        if (!asylumCaseWithPaymentStatus.read(PAYMENT_STATUS, PaymentStatus.class).isPresent()) {
            asylumCaseWithPaymentStatus.write(PAYMENT_STATUS, PAYMENT_DUE);
        }

        return new PreSubmitCallbackResponse<>(asylumCaseWithPaymentStatus);
    }
}
