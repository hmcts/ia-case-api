package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_STARTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_STARTED_BY_ADMIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.PENDING_PAYMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.PAID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.PAYMENT_PENDING;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isRemissionExistsAip;

import java.util.List;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
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
        boolean isPaymentStatusPaid = paymentStatus.isPresent() && (paymentStatus.get() == PAID);

        boolean isDlrmFeeRemission = featureToggler.getValue("dlrm-fee-remission-feature-flag", false);

        boolean isAipJourney = asylumCase
            .read(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.class)
            .map(journeyType -> journeyType == JourneyType.AIP)
            .orElse(false);
        State currentState = callback.getCaseDetails().getState();
        String paAppealTypePaymentOption = asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class).orElse("");
        boolean isPayLaterAppeal = paAppealTypePaymentOption.equals(PAY_LATER);
        if (isDlrmFeeRemission && isAipJourney) {
            return handleDlrmFeeRemission(callback, appealType, currentState, isPayLaterAppeal, isPaymentStatusPaid);
        } else if (isAipJourney && isValidPayLaterPaymentEvent(callback, currentState, isPayLaterAppeal)) {
            return new PreSubmitCallbackResponse<>(asylumCase, currentState);
        } else {
            return decideAppealState(appealType, isPaymentStatusPendingOrFailed, asylumCase);
        }
    }

    private static PreSubmitCallbackResponse<AsylumCase> decideAppealState(AppealType appealType, boolean isPaymentStatusPendingOrFailed, AsylumCase asylumCase) {
        switch (appealType) {
            case EA:
            case HU:
            case EU:
            case AG:
                if (isPaymentStatusPendingOrFailed) {
                    return new PreSubmitCallbackResponse<>(asylumCase, PENDING_PAYMENT);
                }
                return new PreSubmitCallbackResponse<>(asylumCase, APPEAL_SUBMITTED);
            default:
                return new PreSubmitCallbackResponse<>(asylumCase, APPEAL_SUBMITTED);
        }
    }

    private PreSubmitCallbackResponse<AsylumCase> handleDlrmFeeRemission(Callback<AsylumCase> callback, AppealType appealType, State currentState, boolean isPayLaterAppeal, boolean isPaymentStatusPaid) {
        final boolean isDlrmFeeRemissionFlag = featureToggler.getValue("dlrm-fee-remission-feature-flag", false);
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<RemissionOption> remissionOption = asylumCase.read(REMISSION_OPTION, RemissionOption.class);
        Optional<HelpWithFeesOption> helpWithFeesOption = asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class);
        State state = isRemissionExistsAip(remissionOption, helpWithFeesOption, isDlrmFeeRemissionFlag) && !isPayLaterAppeal && !isPaymentStatusPaid && appealType != AppealType.PA
                ? PENDING_PAYMENT : APPEAL_SUBMITTED;
        if (isValidPayLaterPaymentEvent(callback, currentState, isPayLaterAppeal)) {
            state = currentState;
        }
        return new PreSubmitCallbackResponse<>(asylumCase, state);
    }

    private boolean isValidPayLaterPaymentEvent(Callback<AsylumCase> callback, State currentState, boolean isPayLaterAppeal) {
        List<State> invalidStates = List.of(APPEAL_STARTED, APPEAL_STARTED_BY_ADMIN, PENDING_PAYMENT);
        boolean isPaymentEvent = callback.getEvent() == Event.PAYMENT_APPEAL;
        boolean isValidState = !invalidStates.contains(currentState);
        return isPayLaterAppeal && isPaymentEvent && isValidState;
    }

}
