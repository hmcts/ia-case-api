package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
public class RollbackPaymentHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return
            callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && (MOVE_TO_PAYMENT_PENDING == callback.getEvent()
                || ROLLBACK_PAYMENT == callback.getEvent()
                || ROLLBACK_PAYMENT_TIMEOUT == callback.getEvent()
                || ROLLBACK_PAYMENT_TIMEOUT_TO_PAYMENT_PENDING == callback.getEvent()
            );
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

        long caseId = callback
            .getCaseDetails()
            .getId();

        if (callback.getEvent().equals(ROLLBACK_PAYMENT_TIMEOUT) || callback.getEvent().equals(ROLLBACK_PAYMENT_TIMEOUT_TO_PAYMENT_PENDING)) {
            asylumCase.write(AsylumCaseFieldDefinition.PAYMENT_STATUS, PaymentStatus.TIMEOUT);
        } else {
            asylumCase.write(AsylumCaseFieldDefinition.PAYMENT_STATUS, PaymentStatus.FAILED);
        }

        log.info(
            "Triggering event: {} appeal with case ID {} - rollback payment status",
            callback.getEvent().toString(),
            caseId
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}

