package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption.WILL_PAY_FOR_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.*;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Slf4j
@Component
public class PaymentStateHandler implements PreSubmitCallbackStateHandler<AsylumCase> {

    private final boolean isfeePaymentEnabled;
    protected static final String PAY_LATER = "payLater";
    private FeatureToggler featureToggler;

    public PaymentStateHandler(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isfeePaymentEnabled,
        FeatureToggler featureToggler
    ) {
        this.isfeePaymentEnabled = isfeePaymentEnabled;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && (callback.getEvent() == Event.SUBMIT_APPEAL || callback.getEvent() == Event.PAYMENT_APPEAL)
            && isfeePaymentEnabled;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback,
        PreSubmitCallbackResponse<AsylumCase> callbackResponse
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<PaymentStatus> paymentStatus = asylumCase
            .read(PAYMENT_STATUS, PaymentStatus.class);

        Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("Appeal type is not present"));

        log.info("Appeal type [{}] and Remission type [{}] for caseId [{}]",
            appealType, remissionType, callback.getCaseDetails().getId());

        boolean isPaymentStatusPendingOrFailed = paymentStatus.isPresent() && (paymentStatus.get() == PAYMENT_PENDING || paymentStatus.get() == FAILED)
            || (remissionType.isPresent());

        boolean isDlrmFeeRemission = featureToggler.getValue("dlrm-fee-remission-feature-flag", false);
        if (isDlrmFeeRemission) {
            boolean isAipJourney = callback.getCaseDetails().getCaseData()
                .read(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.class)
                .map(journeyType -> journeyType == JourneyType.AIP)
                .orElse(false);
            if (isAipJourney) {
                Optional<RemissionOption> remissionOption = asylumCase.read(REMISSION_OPTION, RemissionOption.class);
                Optional<HelpWithFeesOption> helpWithFeesOption = asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class);
                String paAppealTypePaymentOption = asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class).orElse("");

                if (paAppealTypePaymentOption.equals(PAY_LATER)) {
                    return new PreSubmitCallbackResponse<>(asylumCase, APPEAL_SUBMITTED);
                } else if (isRemissionExistsAip(remissionOption, helpWithFeesOption)) {
                    return new PreSubmitCallbackResponse<>(asylumCase, PENDING_PAYMENT);
                } else {
                    return new PreSubmitCallbackResponse<>(asylumCase, APPEAL_SUBMITTED);
                }
            } else {
                return decideAppealState(appealType, isPaymentStatusPendingOrFailed, asylumCase);
            }

        } else {
            return decideAppealState(appealType, isPaymentStatusPendingOrFailed, asylumCase);
        }
    }

    private static PreSubmitCallbackResponse<AsylumCase> decideAppealState(AppealType appealType, boolean isPaymentStatusPendingOrFailed, AsylumCase asylumCase) {
        switch (appealType) {
            case EA:
            case HU:
            case EU:
                if (isPaymentStatusPendingOrFailed) {
                    return new PreSubmitCallbackResponse<>(asylumCase, PENDING_PAYMENT);
                }
                return new PreSubmitCallbackResponse<>(asylumCase, APPEAL_SUBMITTED);
            default:
                return new PreSubmitCallbackResponse<>(asylumCase, APPEAL_SUBMITTED);
        }
    }

    private boolean isRemissionExistsAip(Optional<RemissionOption> remissionOption, Optional<HelpWithFeesOption> helpWithFeesOption) {
        boolean isDlrmFeeRemission = featureToggler.getValue("dlrm-fee-remission-feature-flag", false);

        return (remissionOption.isPresent() && remissionOption.get() != RemissionOption.NO_REMISSION)
            || (helpWithFeesOption.isPresent() && helpWithFeesOption.get() != WILL_PAY_FOR_APPEAL)
            && isDlrmFeeRemission;
    }
}
