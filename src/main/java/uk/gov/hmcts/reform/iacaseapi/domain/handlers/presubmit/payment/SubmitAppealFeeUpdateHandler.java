package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.sourceOfAppealEjp;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

/**
 * This handler refreshes the fee amount when submitting an appeal.
 * It only calls the payment API to get the latest fee without modifying
 * any remission fields that were already set during start/edit appeal.
 */
@Component
public class SubmitAppealFeeUpdateHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeePayment<AsylumCase> feePayment;
    private final boolean isfeePaymentEnabled;

    public SubmitAppealFeeUpdateHandler(
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

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        List<AppealType> payableAppealTypes = List.of(
            AppealType.EA,
            AppealType.HU,
            AppealType.PA,
            AppealType.EU
        );

        Optional<AppealType> appealType = asylumCase.read(APPEAL_TYPE, AppealType.class);
        boolean isPayableAppealType = appealType.isPresent()
            && payableAppealTypes.contains(appealType.get());

        Optional<YesOrNo> isAcceleratedDetainedAppeal = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class);
        boolean isAda = isAcceleratedDetainedAppeal.isPresent() && isAcceleratedDetainedAppeal.get() == YES;
        boolean isEjp = sourceOfAppealEjp(asylumCase);

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.SUBMIT_APPEAL
            && isfeePaymentEnabled
            && isPayableAppealType
            && !isAda
            && !isEjp;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        // Refresh the fee from the payment API
        AsylumCase asylumCase = feePayment.aboutToSubmit(callback);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
