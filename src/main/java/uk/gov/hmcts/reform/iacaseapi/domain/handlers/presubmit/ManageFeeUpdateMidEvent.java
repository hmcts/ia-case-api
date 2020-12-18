package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeUpdateReason;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class ManageFeeUpdateMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

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
                }
                break;

            case FEE_REMISSION_CHANGED:

                Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

                Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
                Optional<RemissionType> lateRemissionType = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);

                if (!isRemissionExists(remissionType) && !isRemissionExists(lateRemissionType)) {

                    callbackResponse.addError("You cannot choose this option because there is no remission request associated with this appeal");
                } else if (!isRemissionApprovedOrPartiallyApproved(remissionDecision)) {

                    callbackResponse.addError("You cannot choose this option because the remission request has not been decided or has been rejected");
                }
                break;

            default:
                break;
        }

        return callbackResponse;
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
