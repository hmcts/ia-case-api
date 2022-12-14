package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

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
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

@Component
public class FeesAndStatusCheckPreparer implements PreSubmitCallbackHandler<AsylumCase> {

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
            Event.PAYMENT_APPEAL)
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

        if (HandlerUtils.isAipJourney(asylumCase)) {
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        final PaymentStatus paymentStatus = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)
            .orElse(PaymentStatus.PAYMENT_PENDING);

        final String paymentOptionNotAvailableLabel = "The Make a payment option is not available.";

        final String oldOrExistingCasesLabel = "You cannot make a payment for this appeal using Payment by Account";

        YesOrNo isRemissionsEnabled
            = featureToggler.getValue("remissions-feature", false) ? YesOrNo.YES : YesOrNo.NO;
        asylumCase.write(IS_REMISSIONS_ENABLED, isRemissionsEnabled);

        asylumCase.read(APPEAL_TYPE, AppealType.class)
            .ifPresent(type -> {
                Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);
                switch (type) {
                    case EA:
                    case HU:
                    case EU:
                        Optional<String> eaHuPaymentType = asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class);
                        if (eaHuPaymentType.isEmpty()
                            && (remissionDecision.isEmpty() || remissionDecision.get() != RemissionDecision.REJECTED)
                            && (callback.getEvent() == Event.PAYMENT_APPEAL)
                        ) {
                            asylumCasePreSubmitCallbackResponse.addError(oldOrExistingCasesLabel);

                        }

                        eaHuPaymentType
                            .filter(option -> option.equals("payOffline")
                                              || paymentStatus == PaymentStatus.PAID)
                            .ifPresent(s -> {
                                if (callback.getEvent() == Event.PAYMENT_APPEAL) {

                                    asylumCasePreSubmitCallbackResponse.addError(paymentOptionNotAvailableLabel);
                                }
                            });
                        break;

                    case PA:
                        Optional<String> paPaymentType = asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class);
                        if (paPaymentType.isEmpty()
                            && (remissionDecision.isEmpty() || remissionDecision.get() != RemissionDecision.REJECTED)
                            && (callback.getEvent() == Event.PAYMENT_APPEAL)
                        ) {
                            asylumCasePreSubmitCallbackResponse.addError(oldOrExistingCasesLabel);

                        }

                        paPaymentType
                            .filter(option -> option.equals("payOffline")
                                              || paymentStatus == PaymentStatus.PAID
                                              || (option.equals("payLater") && currentState == State.APPEAL_STARTED))
                            .ifPresent(s -> {
                                if (callback.getEvent() == Event.PAYMENT_APPEAL) {

                                    asylumCasePreSubmitCallbackResponse.addError(paymentOptionNotAvailableLabel);
                                }
                            });
                        break;
                    case RP:
                    case DC:
                        if (callback.getEvent() == Event.PAYMENT_APPEAL) {

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
            Event.PAYMENT_APPEAL).contains(callback.getEvent())
        ) {

            asylumCasePreSubmitCallbackResponse.setData(feePayment.aboutToStart(callback));
        }

        return asylumCasePreSubmitCallbackResponse;
    }



}
