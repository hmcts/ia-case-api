package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.FeeDto;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.FeeService;

@Component
public class AppealSubmissionFeeChecker implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeeService feeService;
    private FeeDto feeDto;

    public AppealSubmissionFeeChecker(FeeService feeService) {
        this.feeService = feeService;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START || callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
               && callback.getEvent() == Event.SUBMIT_APPEAL;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("AppealType is not present"));

        if (appealType.equals(AppealType.EA) || appealType.equals(AppealType.HU)) {
            if (!isFeeExists(FeeType.ORAL_FEE)) {
                PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());
                response.addError("Cannot retrieve the fee from fees-register.");

                return response;
            }
            asylumCase.write(APPEAL_FEE_DESC, "The fee for this type of appeal with a hearing is £" + feeDto.getCalculatedAmount());
            asylumCase.write(FEE_AMOUNT_FOR_DISPLAY, "£" + feeDto.getCalculatedAmount());
            asylumCase.write(PAYMENT_STATUS, "Payment due");
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isFeeExists(FeeType feeType) {

        feeDto = feeService.getFee(feeType);
        return feeDto != null;
    }
}
