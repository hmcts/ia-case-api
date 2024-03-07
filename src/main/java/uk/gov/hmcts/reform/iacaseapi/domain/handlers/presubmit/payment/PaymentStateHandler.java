package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption.NO_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.*;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Slf4j
@Component
public class PaymentStateHandler implements PreSubmitCallbackStateHandler<AsylumCase> {

    private final boolean isfeePaymentEnabled;
    private static final String PA_PAY_NOW = "payNow";
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

        switch (appealType) {
            case EA:
            case HU:
            case EU:
                boolean isDlrmFeeRemission = featureToggler.getValue("dlrm-fee-remission-feature-flag", false);
                RemissionOption remissionOption = asylumCase.read(REMISSION_OPTION, RemissionOption.class)
                    .orElse(NO_REMISSION);
                HelpWithFeesOption helpWithFeesOption = asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class)
                    .orElse(HelpWithFeesOption.WILL_PAY_FOR_APPEAL);

                if ((isPaymentStatusPendingOrFailed && !isDlrmFeeRemission)
                    || (isPaymentStatusPendingOrFailed && isDlrmFeeRemission && remissionOption.equals(NO_REMISSION) && helpWithFeesOption.equals(HelpWithFeesOption.WILL_PAY_FOR_APPEAL))) {
                    return new PreSubmitCallbackResponse<>(asylumCase, PENDING_PAYMENT);
                }
                return new PreSubmitCallbackResponse<>(asylumCase, APPEAL_SUBMITTED);
            default:
                return new PreSubmitCallbackResponse<>(asylumCase, APPEAL_SUBMITTED);
        }
    }
}
