package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
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

        AsylumCase asylumCase = feePayment.aboutToSubmit(callback);

        asylumCase.write(AsylumCaseFieldDefinition.IS_FEE_PAYMENT_ENABLED,
            isfeePaymentEnabled ? YesOrNo.YES : YesOrNo.NO);

        if (!asylumCase.read(PAYMENT_STATUS, PaymentStatus.class).isPresent()) {
            asylumCase.write(PAYMENT_STATUS, PAYMENT_PENDING);
        }

        asylumCase.read(APPEAL_TYPE, AppealType.class)
            .ifPresent((appealType) -> {

                switch (appealType) {
                    case EA:
                    case HU:
                        asylumCase.clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
                        break;

                    case PA:
                        asylumCase.clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
                        break;

                    default:
                        String hearingOption = asylumCase.read(RP_DC_APPEAL_HEARING_OPTION, String.class)
                            .orElseThrow(() -> new IllegalStateException("Appeal hearing option is not present"));
                        asylumCase.write(DECISION_HEARING_FEE_OPTION, hearingOption);
                        asylumCase.clear(HEARING_DECISION_SELECTED);
                        asylumCase.clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
                        asylumCase.clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
                        asylumCase.clear(PAYMENT_STATUS);
                }

                feePaymentDisplayProvider.writeDecisionHearingOptionToCaseData(asylumCase);

            });

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
