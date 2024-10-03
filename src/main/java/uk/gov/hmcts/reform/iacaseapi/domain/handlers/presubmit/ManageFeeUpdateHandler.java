package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeTribunalAction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeUpdateReason;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment.FeesHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeeService;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeTribunalAction.REFUND;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeUpdateReason.DECISION_TYPE_CHANGED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

@Component
public class ManageFeeUpdateHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final FeeService feeService;

    public ManageFeeUpdateHandler(FeatureToggler featureToggler, FeeService feeService) {
        this.featureToggler = featureToggler;
        this.feeService = feeService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.MANAGE_FEE_UPDATE
            && featureToggler.getValue("manage-fee-update-feature", false);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<List<String>> completedStages = asylumCase.read(FEE_UPDATE_COMPLETED_STAGES);
        //Cannot repeat same operation
        Set<String> feeUpdateCompleteStages = new LinkedHashSet<>();

        if (completedStages.isPresent()) {
            //Flow 2 or 3 when TCW/Admin is recording Fee update status options/Refund instructed
            List<String> existingCompletedStages = completedStages.get();
            feeUpdateCompleteStages.addAll(existingCompletedStages);

            Optional<CheckValues<String>> maybeFeeUpdateStatus = asylumCase.read(FEE_UPDATE_STATUS);
            maybeFeeUpdateStatus.ifPresent(
                statuses -> statuses.getValues().stream()
                    .filter(feeUpdateStatus -> !existingCompletedStages.contains(feeUpdateStatus))
                    .forEach(feeUpdateCompleteStages::add));

        } else {
            //Flow 1 when TCW/Admin is recording Fee update
            Optional<CheckValues<String>> maybeFeeUpdateRecorded = asylumCase.read(FEE_UPDATE_RECORDED);
            maybeFeeUpdateRecorded.ifPresent(status ->
                feeUpdateCompleteStages.addAll(status.getValues()));
            asylumCase.write(DISPLAY_FEE_UPDATE_STATUS, YES);
        }

        asylumCase.write(
            FEE_UPDATE_COMPLETED_STAGES,
            new ArrayList<>(feeUpdateCompleteStages)
        );

        Optional<FeeUpdateReason> feeUpdateReason = asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class);

        Optional<FeeTribunalAction> feeTribunalAction = asylumCase.read(FEE_UPDATE_TRIBUNAL_ACTION, FeeTribunalAction.class);

        if (feeUpdateReason.isPresent() && feeUpdateReason.get().equals(DECISION_TYPE_CHANGED)
            && feeTribunalAction.isPresent() && feeTribunalAction.get().equals(REFUND)) {
            asylumCase.write(DECISION_TYPE_CHANGED_WITH_REFUND_FLAG, YES);
            FeesHelper.findFeeByHearingType(feeService, asylumCase);
            asylumCase.write(CASE_ARGUMENT_AVAILABLE, YES);
        }

        String decisionHearingFeeOption = asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class).orElse("");
        Optional<String> updatedDecisionHearingFeeOption = asylumCase.read(UPDATED_DECISION_HEARING_FEE_OPTION, String.class);

        if (updatedDecisionHearingFeeOption.isPresent() || feeTribunalAction.isPresent()) {
            String feeAmountGbp = asylumCase.read(FEE_AMOUNT_GBP, String.class).orElse("");
            asylumCase.write(PREVIOUS_FEE_AMOUNT_GBP, feeAmountGbp);
            FeesHelper.findFeeByHearingType(feeService, asylumCase);
            if (updatedDecisionHearingFeeOption.isPresent()) {
                asylumCase.write(PREVIOUS_DECISION_HEARING_FEE_OPTION, decisionHearingFeeOption);
                asylumCase.write(DECISION_HEARING_FEE_OPTION, updatedDecisionHearingFeeOption.get());
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}