package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_WITH_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.sourceOfAppealEjp;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeeService;

/**
 * This handler refreshes the fee amount when submitting an appeal.
 * It fetches the latest fee from the fees register and updates the fee fields
 * without modifying any remission fields that were already set during start/edit appeal.
 */
@Slf4j
@Component
public class SubmitAppealFeeUpdateHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeeService feeService;
    private final boolean isfeePaymentEnabled;

    public SubmitAppealFeeUpdateHandler(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isfeePaymentEnabled,
        FeeService feeService
    ) {
        this.feeService = feeService;
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

        boolean canHandle = callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.SUBMIT_APPEAL
                && isfeePaymentEnabled
                && isPayableAppealType
                && !isAda
                && !isEjp;

        log.info("SubmitAppealFeeUpdateHandler canHandle? : {}, isAda: {}, "
                + "isEjp: {}, isfeePaymentEnabled: {}, isPayableAppealType: {} for caseId: {}",
                canHandle, isAda, isEjp, isfeePaymentEnabled, isPayableAppealType, callback.getCaseDetails().getId());
        return canHandle;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Optional<String> decisionHearingFeeOption = asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class);

        if (decisionHearingFeeOption.isPresent()) {
            FeeType feeType = decisionHearingFeeOption.get().equals("decisionWithHearing")
                ? FeeType.FEE_WITH_HEARING
                : FeeType.FEE_WITHOUT_HEARING;

            Fee fee = feeService.getFee(feeType);

            if (fee != null) {
                log.info("Fee response: fee amount: {} caseId: {}", fee.getAmountAsString(),
                        callback.getCaseDetails().getId());

                // FEE_AMOUNT_GBP stores the amount in pence (as a whole number)
                String feeAmountInPence = fee.getCalculatedAmount()
                    .multiply(new BigDecimal("100"))
                    .setScale(0)
                    .toPlainString();
                asylumCase.write(FEE_AMOUNT_GBP, feeAmountInPence);

                // FEE_WITH_HEARING/FEE_WITHOUT_HEARING store the amount in pounds
                if (feeType == FeeType.FEE_WITH_HEARING) {
                    asylumCase.write(FEE_WITH_HEARING, fee.getAmountAsString());
                } else {
                    asylumCase.write(FEE_WITHOUT_HEARING, fee.getAmountAsString());
                }
            } else {
                log.info("Fee null for case {}", callback.getCaseDetails().getId());
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
