package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

@Component
public class FeePayAndSubmitHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeePayment<AsylumCase> feePayment;
    private final boolean isfeePaymentEnabled;

    public FeePayAndSubmitHandler(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isfeePaymentEnabled,
        FeePayment<AsylumCase> feePayment
    ) {
        this.feePayment = feePayment;
        this.isfeePaymentEnabled = isfeePaymentEnabled;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL
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

        return new PreSubmitCallbackResponse<>(asylumCaseWithPaymentStatus);
    }
}
