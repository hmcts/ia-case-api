package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.AG;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
public class RollbackPaymentPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String NOT_AVAILABLE_LABEL = "You cannot mark this appeal as not paid";

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return
            callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && MOVE_TO_PAYMENT_PENDING == callback.getEvent();
    }


    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback
            .getCaseDetails()
            .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class).orElseThrow(() -> new IllegalStateException("appealType is not set"));

        boolean isEaHuEu = List.of(EA, HU, EU, AG).contains(appealType);

        if (!isEaHuEu) {
            asylumCasePreSubmitCallbackResponse.addError("You cannot mark this type of appeal as unpaid.");
        }

        Optional<String> eaHuPaymentType = asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class);
        Optional<RemissionDecision> optionalRemissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);
        // remisssion is present and remissionDecision is not rejected
        if (isEaHuEu && (eaHuPaymentType.isEmpty()
                && optionalRemissionDecision.isPresent() && optionalRemissionDecision.get() != RemissionDecision.REJECTED)) {
            asylumCasePreSubmitCallbackResponse.addError(NOT_AVAILABLE_LABEL);
        }

        if (isEaHuEu && eaHuPaymentType.isPresent() && eaHuPaymentType.get().equals("payOffline")) {
            asylumCasePreSubmitCallbackResponse.addError(NOT_AVAILABLE_LABEL);
        }

        return asylumCasePreSubmitCallbackResponse;
    }
}

