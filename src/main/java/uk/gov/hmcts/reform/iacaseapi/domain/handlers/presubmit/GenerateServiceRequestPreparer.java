package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAppealPaid;

@Component
public class GenerateServiceRequestPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final boolean isFeePaymentEnabled;
    private final int MAXIMUM_SERVICE_REQUEST_NUMBER_ALLOWED = 2;

    public GenerateServiceRequestPreparer(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isFeePaymentEnabled) {
        this.isFeePaymentEnabled = isFeePaymentEnabled;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START)
            && callback.getEvent() == Event.GENERATE_SERVICE_REQUEST
            && isFeePaymentEnabled;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        if (isAppealPaid(asylumCase) && !refundConfirmationCompleted(asylumCase)) {
            response.addError(
                "A service request has already been paid."
            );
            return response;
        }

        if (Integer.parseInt(asylumCase.read(SERVICE_REQUEST_GENERATED_COUNT, String.class).orElse("0")) >= MAXIMUM_SERVICE_REQUEST_NUMBER_ALLOWED) {
            response.addError(
                "A second service request has already been created for this case."
            );
            return response;
        }

        if (!refundConfirmationCompleted(asylumCase)
            && (!asylumCase.read(SERVICE_REQUEST_REFERENCE, String.class).orElse("").isEmpty()
            || asylumCase.read(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES))
        ) {
            response.addError(
                "A service request has already been created for this case. Pay via the 'Service Request' tab."
            );
            return response;
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean refundConfirmationCompleted(AsylumCase asylumCase) {
        int serviceReqCount = Integer.parseInt(asylumCase.read(SERVICE_REQUEST_GENERATED_COUNT, String.class).orElse("0"));

        return asylumCase.read(REFUND_CONFIRMATION_APPLIED, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES)
            && serviceReqCount < MAXIMUM_SERVICE_REQUEST_NUMBER_ALLOWED;
    }

}