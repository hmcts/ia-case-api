package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_COMPLETED_STAGES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_RECORDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LATE_REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEW_FEE_AMOUNT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.APPROVED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.PARTIALLY_APPROVED;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeUpdateReason;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class ManageFeeUpdateMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String FEE_UPDATE_RECORDED_UNCHECKED = "You must mark Fee update recorded "
        + "before you can select the fee update status";
    private static final String FEE_UPDATE_STATUS_INVALID_SIZE = "You cannot select more than one option at a time";
    private final FeatureToggler featureToggler;

    public ManageFeeUpdateMidEvent(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
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

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
        FeeUpdateReason feeUpdateReason = asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)
            .orElseThrow(() -> new IllegalStateException("Fee update reason is not present"));

        switch (feeUpdateReason) {
            case APPEAL_NOT_VALID:

                String newFeeAmount = asylumCase.read(NEW_FEE_AMOUNT, String.class)
                    .orElseThrow(() -> new IllegalStateException("New fee amount is not present"));

                if (new BigDecimal(newFeeAmount).signum() > 0) {

                    callbackResponse.addError("Appeal not valid is selected, the new fee amount must be 0");
                    return callbackResponse;
                }
                break;

            case FEE_REMISSION_CHANGED:

                Optional<RemissionDecision> remissionDecision =
                    asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

                Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
                Optional<RemissionType> lateRemissionType = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);

                if (!isRemissionExists(remissionType) && !isRemissionExists(lateRemissionType)) {

                    callbackResponse.addError(
                        "You cannot choose this option because there is no remission request associated with this appeal");
                    return callbackResponse;
                } else if (!isRemissionApprovedOrPartiallyApproved(remissionDecision)) {

                    callbackResponse.addError(
                        "You cannot choose this option because the remission request has not been decided or has been rejected");
                    return callbackResponse;
                }
                break;

            default:
                break;
        }

        Optional<CheckValues<String>> maybeFeeUpdateRecorded = asylumCase.read(FEE_UPDATE_RECORDED);
        Optional<CheckValues<String>> maybeFeeUpdateStatus = asylumCase.read(FEE_UPDATE_STATUS);
        Optional<List<String>> completedStages = asylumCase.read(FEE_UPDATE_COMPLETED_STAGES);

        if (maybeFeeUpdateStatus.isPresent() && maybeFeeUpdateStatus.get().getValues().size() > 1) {
            callbackResponse.addError(FEE_UPDATE_STATUS_INVALID_SIZE);
            return callbackResponse;
        }

        boolean isFeeUpdateRecorded = isFeeUpdateStatusChecked(maybeFeeUpdateRecorded, "feeUpdateRecorded");
        boolean isRefundApproved = isFeeUpdateStatusChecked(maybeFeeUpdateStatus, "feeUpdateRefundApproved");
        boolean isRefundInstructed = isFeeUpdateStatusChecked(maybeFeeUpdateStatus, "feeUpdateRefundInstructed");
        boolean isAdditionFeeRequested =
            isFeeUpdateStatusChecked(maybeFeeUpdateStatus, "feeUpdateAdditionalFeeRequested");
        boolean isFeeUpdateNotRequired = isFeeUpdateStatusChecked(maybeFeeUpdateStatus, "feeUpdateNotRequired");
        boolean completeStagesHasFeesUpdateRecorded = completedStagesHasFeeUpdateStatus(
            completedStages, "feeUpdateRecorded");

        //At least one option should be chosen when Fee Update Recorded
        if (completeStagesHasFeesUpdateRecorded
            && isFeeUpdateRecorded
            && !(isRefundApproved
            || isRefundInstructed
            || isAdditionFeeRequested
            || isFeeUpdateNotRequired)) {
            callbackResponse.addError(FEE_UPDATE_STATUS_INVALID_SIZE);
            return callbackResponse;
        }

        //Fee Update Recorded should be checked for any option
        if ((isRefundApproved
            || isRefundInstructed
            || isAdditionFeeRequested
            || isFeeUpdateNotRequired)
            && (!completeStagesHasFeesUpdateRecorded
            || !isFeeUpdateRecorded)
        ) {
            callbackResponse.addError(FEE_UPDATE_RECORDED_UNCHECKED);
            return callbackResponse;
        }

        if (isRefundInstructed
            && (!completedStagesHasFeeUpdateStatus(completedStages, "feeUpdateRefundApproved")
            || !isRefundApproved)
        ) {
            callbackResponse.addError("You must select refund approved before you can mark a refund as instructed");
            return callbackResponse;
        }

        //Check Decision based selection
        switch (feeUpdateReason) {
            case APPEAL_NOT_VALID:
            case FEE_REMISSION_CHANGED:
                if (isAdditionFeeRequested) {
                    callbackResponse.addError("You cannot select additional fee requested for this type of fee update."
                        + " It can only be a refund.");
                }
                break;
            default:
                break;
        }

        return callbackResponse;
    }

    private boolean isFeeUpdateStatusChecked(
        Optional<CheckValues<String>> maybeFeeUpdateStatus, String feeUpdateStatus) {
        AtomicBoolean isStatusChecked = new AtomicBoolean(false);
        maybeFeeUpdateStatus.ifPresent(
            statuses -> {
                isStatusChecked.set(statuses.getValues().stream()
                    .anyMatch(value -> value.equals(feeUpdateStatus)));
            }
        );
        return isStatusChecked.get();
    }

    private boolean completedStagesHasFeeUpdateStatus(
        Optional<List<String>> completedStages, String preRequisiteFeeUpdateStatus) {
        AtomicBoolean preRequisiteFeeUpdateStatusExists = new AtomicBoolean(false);

        completedStages.ifPresent(
            stages -> preRequisiteFeeUpdateStatusExists.set(
                stages.contains(preRequisiteFeeUpdateStatus)));

        return preRequisiteFeeUpdateStatusExists.get();
    }

    private boolean isRemissionApprovedOrPartiallyApproved(Optional<RemissionDecision> remissionDecision) {

        return remissionDecision.isPresent()
            && Arrays.asList(APPROVED, PARTIALLY_APPROVED)
            .contains(remissionDecision.get());
    }

    private boolean isRemissionExists(Optional<RemissionType> remissionType) {

        return remissionType.isPresent()
            && remissionType.get() != RemissionType.NO_REMISSION;
    }
}
