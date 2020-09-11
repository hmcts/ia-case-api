package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EA_HU_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_DECISION_SELECTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.PAYMENT_PENDING;

import java.util.Arrays;
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
               && Arrays.asList(
                   Event.START_APPEAL,
                   Event.EDIT_APPEAL,
                   Event.PAYMENT_APPEAL
                ).contains(callback.getEvent())
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
            isfeePaymentEnabled ? YesOrNo.YES : YesOrNo.NO);

        if (!asylumCaseWithPaymentStatus.read(PAYMENT_STATUS, PaymentStatus.class).isPresent()) {
            asylumCaseWithPaymentStatus.write(PAYMENT_STATUS, PAYMENT_PENDING);
        }

        asylumCaseWithPaymentStatus.read(APPEAL_TYPE, AppealType.class)
            .ifPresent((appealType) -> {

                switch (appealType) {
                    case EA:
                    case HU:
                        asylumCaseWithPaymentStatus.clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
                        break;

                    case PA:
                        asylumCaseWithPaymentStatus.clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
                        break;

                    default:
                        asylumCaseWithPaymentStatus.clear(DECISION_HEARING_FEE_OPTION);
                        asylumCaseWithPaymentStatus.clear(HEARING_DECISION_SELECTED);
                        asylumCaseWithPaymentStatus.clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
                        asylumCaseWithPaymentStatus.clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
                        asylumCaseWithPaymentStatus.clear(PAYMENT_STATUS);
                }

                feePaymentDisplayProvider.writeDecisionHearingOptionToCaseData(asylumCaseWithPaymentStatus);

            });

        return new PreSubmitCallbackResponse<>(asylumCaseWithPaymentStatus);
    }
}
