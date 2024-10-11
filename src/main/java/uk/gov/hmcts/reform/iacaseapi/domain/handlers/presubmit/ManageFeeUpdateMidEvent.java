package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_COMPLETED_STAGES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HELP_WITH_FEES_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LATE_REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEW_FEE_AMOUNT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.APPROVED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.PARTIALLY_APPROVED;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAipJourney;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isRemissionExists;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isRemissionExistsAip;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeUpdateReason;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption;
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

    private static final String FEE_UPDATE_STATUS_INVALID_SIZE = "You cannot select more than one option at a time";
    private static final String REFUND_APPROVED = "feeUpdateRefundApproved";
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

                final boolean isDlrmFeeRemissionFlag = featureToggler.getValue("dlrm-fee-remission-feature-flag", false);
                Optional<RemissionOption> remissionOption = asylumCase.read(REMISSION_OPTION, RemissionOption.class);
                Optional<HelpWithFeesOption> helpWithFeesOption = asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class);

                if ((!isAipJourney(asylumCase) && !isRemissionExists(remissionType) && !isRemissionExists(lateRemissionType))
                    || (isAipJourney(asylumCase) && !isRemissionExistsAip(remissionOption, helpWithFeesOption, isDlrmFeeRemissionFlag))) {

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

        /*
            USER FLOW
            Flow 1: Mark Fee update recorded
            Flow 2: Select ONLY one of these - Refund approved, Additional fee requested, Fee update not required
                    Flow 2 can be repeated many times. Select Additional fee and then Fee update not required
                    Flow 3 is valid only for Refund approved
            Flow 3: Refund instructed after Refund approved
                    User can continue after Flow 2, Flow 3 completion
            Other flows after Flow 3 are defined
         */
        Optional<CheckValues<String>> maybeFeeUpdateStatus = asylumCase.read(FEE_UPDATE_STATUS);
        Optional<List<String>> completedStages = asylumCase.read(FEE_UPDATE_COMPLETED_STAGES);

        //Flow 2 and Flow 3
        if (maybeFeeUpdateStatus.isPresent()) {

            String lastCompletedStep = "";
            if (completedStages.isPresent()) {
                lastCompletedStep = completedStages.get().get(completedStages.get().size() - 1);
            }

            //Flow 2: only one Fee update status should be checked right after Fee update recorded
            //Flow 2: At least one option should be chosen when Fee Update Recorded (Handled in CCD)
            if (completedStages.isPresent() && completedStages.get().size() == 1
                && maybeFeeUpdateStatus.get().getValues().size() > 1
            ) {
                callbackResponse.addError(FEE_UPDATE_STATUS_INVALID_SIZE);
                return callbackResponse;
            }

            boolean isAdditionFeeRequested =
                isFeeUpdateStatusChecked(maybeFeeUpdateStatus, "feeUpdateAdditionalFeeRequested");
            //Check Decision based selection
            switch (feeUpdateReason) {
                case APPEAL_NOT_VALID:
                case FEE_REMISSION_CHANGED:
                    if (isAdditionFeeRequested) {
                        callbackResponse
                            .addError("You cannot select additional fee requested for this type of fee update."
                                + " It can only be a refund.");
                    }
                    break;
                default:
                    break;
            }

            //Flow 2 and 3: You cannot select Refund instructed, valid only for Flow 3
            boolean isRefundApproved = isFeeUpdateStatusChecked(maybeFeeUpdateStatus, REFUND_APPROVED);
            boolean isRefundInstructed = isFeeUpdateStatusChecked(maybeFeeUpdateStatus, "feeUpdateRefundInstructed");
            if (isRefundInstructed
                && (!completedStagesHasFeeUpdateStatus(completedStages, REFUND_APPROVED))
            ) {
                callbackResponse.addError("You must select refund approved before you can mark a refund as instructed");
                return callbackResponse;
            }

            boolean isFeeUpdateNotRequired = isFeeUpdateStatusChecked(maybeFeeUpdateStatus, "feeUpdateNotRequired");
            //Flow 3: One of the 2 valid options must be selected
            //Flow 3: Both valid options must NOT be selected
            //Flow 3: previous selection Refund approved should not be unchecked, covered above in Flow 2
            if (lastCompletedStep.equals(REFUND_APPROVED)) {
                if (!(isRefundInstructed || isFeeUpdateNotRequired)
                    || (isRefundInstructed && isFeeUpdateNotRequired)
                    || isAdditionFeeRequested) {
                    callbackResponse.addError(
                        "You cannot make this selection. "
                            + "Select either refund instructed or fee not required to continue.");
                    return callbackResponse;
                }
                if (!isRefundApproved) {
                    callbackResponse.addError(
                        "This selection is not valid. You cannot deselect an option that has already been selected");
                    return callbackResponse;
                }
            }

            if (lastCompletedStep.equals("feeUpdateRefundInstructed")) {
                if (!(isAdditionFeeRequested || isFeeUpdateNotRequired)
                    || (isAdditionFeeRequested && isFeeUpdateNotRequired)) {
                    callbackResponse.addError(
                        "This selection is not valid. "
                            + "You must select either fee update not required or additional fee requested to continue");
                    return callbackResponse;
                }
                if (!(isRefundApproved && isRefundInstructed)) {
                    callbackResponse.addError(
                        "This selection is not valid. You cannot deselect an option that has already been selected");
                    return callbackResponse;
                }
            }
        }

        return callbackResponse;
    }

    private boolean isFeeUpdateStatusChecked(
        Optional<CheckValues<String>> maybeFeeUpdateStatus, String feeUpdateStatus) {
        AtomicBoolean isStatusChecked = new AtomicBoolean(false);
        maybeFeeUpdateStatus.ifPresent(
            statuses -> isStatusChecked.set(statuses.getValues().stream()
                .anyMatch(value -> value.equals(feeUpdateStatus)))
        );
        return isStatusChecked.get();
    }

    private boolean isRemissionApprovedOrPartiallyApproved(Optional<RemissionDecision> remissionDecision) {

        return remissionDecision.isPresent()
            && Arrays.asList(APPROVED, PARTIALLY_APPROVED)
            .contains(remissionDecision.get());
    }

    private boolean completedStagesHasFeeUpdateStatus(
        Optional<List<String>> completedStages, String preRequisiteFeeUpdateStatus) {
        AtomicBoolean preRequisiteFeeUpdateStatusExists = new AtomicBoolean(false);

        completedStages.ifPresent(
            stages -> preRequisiteFeeUpdateStatusExists.set(
                stages.contains(preRequisiteFeeUpdateStatus)));

        return preRequisiteFeeUpdateStatusExists.get();
    }

}
