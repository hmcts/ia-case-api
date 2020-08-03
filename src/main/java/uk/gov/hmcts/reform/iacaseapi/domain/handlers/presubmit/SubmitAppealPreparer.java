package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAY_FOR_THE_APPEAL_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class SubmitAppealPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final boolean isfeePaymentEnabled;

    public SubmitAppealPreparer(
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

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START)
               && Arrays.asList(
                    Event.SUBMIT_APPEAL)
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

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);

        if (this.isPaymentAppealTypePayNow(asylumCase)) {
            asylumCasePreSubmitCallbackResponse.addError("The Submit your appeal option is not available. Select Pay and submit to submit the appeal");
        }

        return asylumCasePreSubmitCallbackResponse;
    }

    public boolean isPaymentAppealTypePayNow(AsylumCase asylumCase) {
        boolean foundPaymentAppealTypePayNow = false;
        String payNowOption = "payNow";

        Optional<AppealType> appealType = asylumCase.read(APPEAL_TYPE);
        if (appealType.isPresent()) {
            if (appealType.get().equals(AppealType.EA)
                || appealType.get().equals(AppealType.HU)) {
                foundPaymentAppealTypePayNow = asylumCase.read(PAY_FOR_THE_APPEAL_OPTION, String.class).orElse("").equals(payNowOption);
            }
            if (appealType.get().equals(AppealType.PA)) {
                foundPaymentAppealTypePayNow = asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("").equals(payNowOption);
            }
        }
        return foundPaymentAppealTypePayNow;
    }

}
