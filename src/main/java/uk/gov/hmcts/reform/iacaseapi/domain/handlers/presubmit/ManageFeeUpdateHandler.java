package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_ARGUMENT_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DISPLAY_FEE_UPDATE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_COMPLETED_STAGES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_RECORDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeUpdateReason.DECISION_TYPE_CHANGED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeUpdateReason;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class ManageFeeUpdateHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;

    public ManageFeeUpdateHandler(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
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

        FeeUpdateReason feeUpdateReason = asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)
            .orElseThrow(() -> new IllegalStateException("Fee update reason is not present"));

        if (DECISION_TYPE_CHANGED == feeUpdateReason) {
            asylumCase.write(CASE_ARGUMENT_AVAILABLE, YES);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}