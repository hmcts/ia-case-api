package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.*;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SuperAppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;

@Slf4j
@Component
public class PaymentStateHandler implements PreSubmitCallbackStateHandler<AsylumCase> {

    private final boolean isfeePaymentEnabled;
    private static final String PA_PAY_NOW = "payNow";

    public PaymentStateHandler(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isfeePaymentEnabled
    ) {
        this.isfeePaymentEnabled = isfeePaymentEnabled;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        boolean isRepJourney = callback.getCaseDetails().getCaseData()
                .read(JOURNEY_TYPE, JourneyType.class)
                .map(j -> j == JourneyType.REP)
                .orElse(true);

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.SUBMIT_APPEAL || callback.getEvent() == Event.PAYMENT_APPEAL)
               && isRepJourney
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

        SuperAppealType superAppealType = SuperAppealType.mapFromAsylumCaseAppealType(asylumCase)
            .orElseThrow(() -> new IllegalStateException("Appeal type or Super appeal type not present"));

        log.info("Appeal type [{}] and Remission type [{}] for caseId [{}]",
                superAppealType, remissionType, callback.getCaseDetails().getId());

        boolean isPaymentStatusPendingOrFailed = paymentStatus.isPresent() && (paymentStatus.get() == PAYMENT_PENDING || paymentStatus.get() == FAILED)
                                                                 || (remissionType.isPresent());

        switch (superAppealType) {
            case EA:
            case HU:
                if (isPaymentStatusPendingOrFailed) {
                    return new PreSubmitCallbackResponse<>(asylumCase, PENDING_PAYMENT);
                }
                return new PreSubmitCallbackResponse<>(asylumCase, APPEAL_SUBMITTED);
            default:
                return new PreSubmitCallbackResponse<>(asylumCase, APPEAL_SUBMITTED);
        }
    }
}
