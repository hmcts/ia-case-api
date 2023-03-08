package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.*;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class MarkPaymentPaidPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String NOT_AVAILABLE_LABEL = "You cannot mark this appeal as paid";

    private final boolean isFeePaymentEnabled;
    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;

    public MarkPaymentPaidPreparer(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isFeePaymentEnabled,
        UserDetails userDetails, UserDetailsHelper userDetailsHelper
    ) {
        this.isFeePaymentEnabled = isFeePaymentEnabled;
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
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

        if (isTribunalCaseworker() && !internalDetainedCase(asylumCase)) {
            callbackResponse.addError("You don't have required access to perform this task");
            return callbackResponse;
        }

        if (HandlerUtils.isAppealPaid(asylumCase)) {
            callbackResponse.addError("The fee for this appeal has already been paid.");
        }

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("Appeal type is not present"));

        switch (appealType) {
            case EA:
            case HU:
            case PA:
            case AG:
            case EU:
                if (internalDetainedCase(asylumCase)) {
                    if (awaitingRemissionDecision(asylumCase)) {

                        callbackResponse.addError("You cannot mark this appeal as paid because the remission decision has not been recorded.");
                    } else if (remissionDecisionApproved(asylumCase)) {

                        callbackResponse.addError("You cannot mark this appeal as paid because a full remission has been approved.");
                    }
                } else {
                    Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);

                    Optional<String> paPaymentType = asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class);
                    Optional<PaymentStatus> paymentStatus = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class);
                    Optional<String> eaHuPaymentType = asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class);
                    boolean isEaHuEuAg = List.of(EA, HU, EU, AG).contains(appealType);
                    // old cases
                    if ((appealType == PA && remissionType.isEmpty() && paPaymentType.isEmpty())
                        || (isEaHuEuAg && remissionType.isEmpty()
                        && eaHuPaymentType.isEmpty())) {
                        callbackResponse.addError(NOT_AVAILABLE_LABEL);
                    }
                    if (isEaHuEuAg && (remissionType.isEmpty() || remissionType.get() == NO_REMISSION)
                        && eaHuPaymentType.isPresent() && eaHuPaymentType.get().equals("payNow")
                        && paymentStatus.isPresent() && paymentStatus.get() == PaymentStatus.PAID) {
                        callbackResponse.addError(NOT_AVAILABLE_LABEL);
                    }

                    if (appealType == PA && remissionType.isPresent()
                        && remissionType.get() == NO_REMISSION
                        && HandlerUtils.isAipJourney(asylumCase)) {
                        paPaymentType
                            .filter(option -> option.equals("payLater"))
                            .ifPresent(s ->
                                callbackResponse.addError(NOT_AVAILABLE_LABEL)
                            );
                    } else if (awaitingRemissionDecision(asylumCase)) {

                        callbackResponse.addError("You cannot mark this appeal as paid because the remission decision has not been recorded.");
                    } else if (remissionDecisionApproved(asylumCase)) {

                        callbackResponse.addError("You cannot mark this appeal as paid because a full remission has been approved.");
                    }
                }
                break;

            case RP:
            case DC:
                callbackResponse.addError("Payment is not required for this type of appeal.");
                break;

            default:
                break;
        }

        return callbackResponse;
    }

    private boolean awaitingRemissionDecision(AsylumCase asylumCase) {
        Optional<RemissionType> lateRemissionType = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);
        Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
        Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);
        return (remissionType.isPresent() && remissionType.get() != NO_REMISSION && remissionDecision.isEmpty())
            || (lateRemissionType.isPresent() && remissionDecision.isEmpty());
    }

    private boolean remissionDecisionApproved(AsylumCase asylumCase) {
        Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);
        return remissionDecision.map(decision -> APPROVED == decision).orElse(false);
    }

    private boolean internalDetainedCase(AsylumCase asylumCase) {
        return HandlerUtils.isInternalCase(asylumCase) && HandlerUtils.isAppellantInDetention(asylumCase);
    }

    private boolean isTribunalCaseworker() {
        return userDetailsHelper.getLoggedInUserRoleLabel(userDetails).equals(UserRoleLabel.TRIBUNAL_CASEWORKER);
    }
}
