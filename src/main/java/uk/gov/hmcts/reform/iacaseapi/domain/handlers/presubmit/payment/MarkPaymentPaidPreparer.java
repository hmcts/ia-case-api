package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.APPROVED;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isRemissionExists;

@Component
public class MarkPaymentPaidPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String NOT_AVAILABLE_LABEL = "You cannot mark this appeal as paid";

    private final boolean isFeePaymentEnabled;
    private final FeatureToggler featureToggler;


    public MarkPaymentPaidPreparer(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isFeePaymentEnabled,
        FeatureToggler featureToggler
    ) {
        this.isFeePaymentEnabled = isFeePaymentEnabled;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && callback.getEvent() == Event.MARK_APPEAL_PAID
            && isFeePaymentEnabled;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final PreSubmitCallbackResponse<AsylumCase> callbackResponse = new PreSubmitCallbackResponse<>(asylumCase);

        if (HandlerUtils.isAppealPaid(asylumCase)) {
            callbackResponse.addError("The fee for this appeal has already been paid.");
        }

        checkRemissionConditions(asylumCase, callbackResponse);

        return callbackResponse;
    }

    //good example
    private void checkRemissionConditions(AsylumCase asylumCase, PreSubmitCallbackResponse<AsylumCase> callbackResponse) {
        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("Appeal type is not present"));

        final boolean isAipJourney = asylumCase.read(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.class)
            .map(journeyType -> journeyType == JourneyType.AIP)
            .orElse(false);

        switch (appealType) {
            case EA, HU, PA, AG, EU -> {
                boolean isDlrmFeeRemission = featureToggler.getValue("dlrm-fee-remission-feature-flag", false);
                Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
                Optional<RemissionType> lateRemissionType = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);

                if (isDlrmFeeRemission) {
                    checkPaymentConditions(appealType, asylumCase, callbackResponse, remissionType, lateRemissionType, isAipJourney);
                } else if (HandlerUtils.isAcceleratedDetainedAppeal(asylumCase)) {
                    callbackResponse.addError("Payment is not required for this type of appeal.");
                } else if (internalDetainedCase(asylumCase)) {
                    if (awaitingRemissionDecision(asylumCase, remissionType, lateRemissionType)) {
                        callbackResponse.addError("You cannot mark this appeal as paid because the remission decision has not been recorded.");
                    } else if (remissionDecisionApproved(asylumCase)) {
                        callbackResponse.addError("You cannot mark this appeal as paid because a full remission has been approved.");
                    }
                } else {
                    checkPaymentConditions(appealType, asylumCase, callbackResponse, remissionType, lateRemissionType, isAipJourney);
                }
            }

            case RP, DC -> callbackResponse.addError("Payment is not required for this type of appeal.");

            default -> callbackResponse.addError("Unknown appeal type.");
        }
    }

    private void checkPaymentConditions(AppealType appealType, AsylumCase asylumCase, PreSubmitCallbackResponse<AsylumCase> callbackResponse, Optional<RemissionType> remissionType, Optional<RemissionType> lateRemissionType, boolean isAipJourney) {
        Optional<String> paPaymentType = isAipJourney
            ? asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)
            : asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class);
        Optional<PaymentStatus> paymentStatus = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class);
        boolean isEaHuEuAg = List.of(EA, HU, EU, AG).contains(appealType);

        // old cases
        if (!isRemissionExists(remissionType)
            && !isRemissionExists(lateRemissionType)
            && (appealType == PA && paPaymentType.isEmpty())) {
            callbackResponse.addError(NOT_AVAILABLE_LABEL);
        }

        if (!isRemissionExists(remissionType)
            && !isRemissionExists(lateRemissionType)
            && isEaHuEuAg
            && paymentStatus.isPresent() && paymentStatus.get() == PaymentStatus.PAID) {
            callbackResponse.addError(NOT_AVAILABLE_LABEL);
        } else if (awaitingRemissionDecision(asylumCase, remissionType, lateRemissionType)) {
            callbackResponse.addError("You cannot mark this appeal as paid because the remission decision has not been recorded.");
        } else if (remissionDecisionApproved(asylumCase)) {
            callbackResponse.addError("You cannot mark this appeal as paid because a full remission has been approved.");
        }
    }

    private boolean awaitingRemissionDecision(AsylumCase asylumCase, Optional<RemissionType> remissionType, Optional<RemissionType> lateRemissionType) {
        Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);
        return ((isRemissionExists(remissionType) || isRemissionExists(lateRemissionType)) && remissionDecision.isEmpty());
    }

    private boolean remissionDecisionApproved(AsylumCase asylumCase) {
        Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);
        return remissionDecision.map(decision -> APPROVED == decision).orElse(false);
    }

    private boolean internalDetainedCase(AsylumCase asylumCase) {
        return HandlerUtils.isInternalCase(asylumCase) && HandlerUtils.isAppellantInDetention(asylumCase);
    }
}
