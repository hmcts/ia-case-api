package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption.NO_REMISSION;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

@Component
public class RecordRemissionDecisionPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeePayment<AsylumCase> feePayment;
    private final FeatureToggler featureToggler;

    public RecordRemissionDecisionPreparer(
        FeePayment<AsylumCase> feePayment,
        FeatureToggler featureToggler
    ) {
        this.feePayment = feePayment;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.RECORD_REMISSION_DECISION
               && featureToggler.getValue("remissions-feature", false);
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

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse = new PreSubmitCallbackResponse<>(asylumCase);

        final AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("Appeal type is not present"));

        switch (appealType) {
            case EA:
            case HU:
            case EU:
            case PA:
                Optional<PaymentStatus> paymentStatus = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class);

                Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
                Optional<RemissionType> lateRemissionType = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);

                Optional<RemissionOption> remissionOptionAip = asylumCase.read(REMISSION_OPTION, RemissionOption.class);

                Optional<RemissionDecision> remissionDecision =
                    asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

                if (!isRemissionExists(remissionType) && !isRemissionExists(lateRemissionType) && !isRemissionExistsAip(remissionOptionAip)) {

                    callbackResponse.addError("You cannot record a remission decision because a remission has not been requested for this appeal");

                } else if (isRemissionAmountLeftPaid(remissionDecision, paymentStatus)) {

                    callbackResponse.addError("The fee for this appeal has already been paid.");

                } else if (remissionDecision.isPresent()
                           && Arrays.asList(APPROVED, PARTIALLY_APPROVED, REJECTED).contains(remissionDecision.get())) {

                    callbackResponse.addError("The remission decision for this appeal has already been recorded.");

                } else {

                    callbackResponse.setData(feePayment.aboutToStart(callback));
                }

                break;

            case DC:
            case RP:
                callbackResponse.addError("Record remission decision is not valid for the appeal type.");
                break;

            default:
                break;
        }

        return callbackResponse;
    }

    private boolean isRemissionExists(Optional<RemissionType> remissionType) {

        return remissionType.isPresent() && remissionType.get() != RemissionType.NO_REMISSION;
    }

    private boolean isRemissionExistsAip(Optional<RemissionOption> remissionOption) {
        boolean isDlrmFeeRemission = featureToggler.getValue("dlrm-fee-remission-feature-flag", false);
        return remissionOption.isPresent() && remissionOption.get() != NO_REMISSION && isDlrmFeeRemission;
    }

    private boolean isRemissionAmountLeftPaid(
        Optional<RemissionDecision> remissionDecision, Optional<PaymentStatus> paymentStatus
    ) {

        return remissionDecision.isPresent()
               && Arrays.asList(PARTIALLY_APPROVED, REJECTED).contains(remissionDecision.get())
               && paymentStatus.isPresent()
               && Arrays.asList(PaymentStatus.PAID).contains(paymentStatus.get());
    }
}
