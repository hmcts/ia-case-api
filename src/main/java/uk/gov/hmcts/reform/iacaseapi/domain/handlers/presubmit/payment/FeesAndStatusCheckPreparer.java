package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FEE_PAYMENT_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_REMISSIONS_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_AIP_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REFUND_CONFIRMATION_APPLIED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.REJECTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.PAYMENT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_STARTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.PAID;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

@Component
public class FeesAndStatusCheckPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String PAYMENT_OPTION_NOT_AVAILABLE_LABEL = "The Make a payment option is not available.";
    private static final String OLD_OR_EXISTING_CASES_LABEL = "You cannot make a payment for this appeal using Payment by Account";
    private final FeePayment<AsylumCase> feePayment;
    private final FeatureToggler featureToggler;
    private final boolean isfeePaymentEnabled;

    public FeesAndStatusCheckPreparer(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isfeePaymentEnabled,
        FeatureToggler featureToggler,
        FeePayment<AsylumCase> feePayment
    ) {
        this.feePayment = feePayment;
        this.featureToggler = featureToggler;
        this.isfeePaymentEnabled = isfeePaymentEnabled;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START)
               && Arrays.asList(
            Event.START_APPEAL,
            Event.EDIT_APPEAL,
            PAYMENT_APPEAL)
                   .contains(callback.getEvent())
               && isfeePaymentEnabled;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final State currentState = callback.getCaseDetails().getState();

        final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse
            = new PreSubmitCallbackResponse<>(asylumCase);

        final PaymentStatus paymentStatus = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)
            .orElse(PaymentStatus.PAYMENT_PENDING);

        YesOrNo isRemissionsEnabled
            = featureToggler.getValue("remissions-feature", false) ? YesOrNo.YES : YesOrNo.NO;
        asylumCase.write(IS_REMISSIONS_ENABLED, isRemissionsEnabled);

        Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

        boolean remissionPartiallyApproved = remissionDecision.isPresent()
                                             && remissionDecision.get().equals(RemissionDecision.PARTIALLY_APPROVED);

        boolean remissionApproved = remissionDecision.isPresent()
                                    && remissionDecision.get().equals(RemissionDecision.APPROVED);

        boolean paymentAppealEvent = callback.getEvent().equals(PAYMENT_APPEAL);
        boolean isPaid = paymentStatus.equals(PAID);

        asylumCase.read(APPEAL_TYPE, AppealType.class)
            .ifPresent(type -> {
                switch (type) {
                    case EA:
                    case HU:
                    case EU:
                    case AG:
                        if ((isPaid && !asylumCase.read(REFUND_CONFIRMATION_APPLIED, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES))
                            || remissionPartiallyApproved
                            || remissionApproved) {
                            asylumCasePreSubmitCallbackResponse.addError(PAYMENT_OPTION_NOT_AVAILABLE_LABEL);
                        }
                        break;

                    case PA:
                        String paPaymentType = asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");
                        String paAipPaymentType = asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class).orElse("");

                        if (paPaymentType.isEmpty()
                            && paAipPaymentType.isEmpty()
                            && (remissionDecision.isEmpty() || remissionDecision.get() != REJECTED)
                            && paymentAppealEvent
                        ) {
                            asylumCasePreSubmitCallbackResponse.addError(OLD_OR_EXISTING_CASES_LABEL);
                        }

                        boolean paAppealStartedPayLater = (paPaymentType.equals("payLater") || paAipPaymentType.equals("payLater")) && currentState == APPEAL_STARTED;

                        if (((isPaid && !asylumCase.read(REFUND_CONFIRMATION_APPLIED, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES))
                            || remissionPartiallyApproved
                            || remissionApproved
                            || paAppealStartedPayLater)
                            && paymentAppealEvent) {
                            asylumCasePreSubmitCallbackResponse.addError(PAYMENT_OPTION_NOT_AVAILABLE_LABEL);
                        }

                        break;
                    case RP:
                    case DC:
                        if (callback.getEvent() == PAYMENT_APPEAL) {

                            asylumCasePreSubmitCallbackResponse
                                .addError("You do not have to pay for this type of appeal.");
                        }
                        break;
                    default:
                        break;

                }
            });


        asylumCase.write(IS_FEE_PAYMENT_ENABLED, YesOrNo.YES);

        if (asylumCasePreSubmitCallbackResponse.getErrors().isEmpty()
            && Arrays.asList(
            PAYMENT_APPEAL).contains(callback.getEvent())
        ) {

            asylumCasePreSubmitCallbackResponse.setData(feePayment.aboutToStart(callback));
        }

        return asylumCasePreSubmitCallbackResponse;
    }



}
