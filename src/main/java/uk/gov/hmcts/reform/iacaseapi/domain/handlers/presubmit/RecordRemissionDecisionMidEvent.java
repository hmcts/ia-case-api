package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.*;

import java.math.BigDecimal;
import java.util.Arrays;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class RecordRemissionDecisionMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String GBP = "GBP";
    private final FeatureToggler featureToggler;

    public RecordRemissionDecisionMidEvent(
        FeatureToggler featureToggler
    ) {
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == Event.RECORD_REMISSION_DECISION
               && featureToggler.getValue("remissions-feature", false);
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

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = new PreSubmitCallbackResponse<>(asylumCase);

        RemissionDecision remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
            .orElseThrow(() -> new IllegalStateException("Remission decision is not present"));

        String decisionHearingFeeOption = asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class).orElse("");
        String feeAmount = decisionHearingFeeOption.equals("decisionWithHearing")
            ? asylumCase.read(FEE_WITH_HEARING, String.class)
                .orElseThrow(() -> new IllegalStateException("Fee with hearing is not present"))
            : asylumCase.read(FEE_WITHOUT_HEARING, String.class)
                .orElseThrow(() -> new IllegalStateException("Fee without hearing is not present"));

        Money feeAmountInGbp = Money.of(new BigDecimal(feeAmount), GBP);
        asylumCase.write(FEE_AMOUNT_GBP, String.valueOf(new BigDecimal(feeAmount).multiply(new BigDecimal("100"))));

        if (Arrays.asList(APPROVED, PARTIALLY_APPROVED).contains(remissionDecision)) {

            String amountRemitted = asylumCase.read(AMOUNT_REMITTED, String.class)
                .orElseThrow(() -> new IllegalStateException("Amount remitted is not present"));
            String amountLeftToPay = asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)
                .orElseThrow(() -> new IllegalStateException("Amount left to pay is not present"));

            Money amountRemittedInGbp = Money.of(new BigDecimal(String.valueOf(Double.valueOf(amountRemitted) / 100)), GBP);
            Money amountLeftToPayInGbp = Money.of(new BigDecimal(String.valueOf(Double.valueOf(amountLeftToPay) / 100)), GBP);

            switch (remissionDecision) {
                case APPROVED:
                    if (!amountRemittedInGbp.isEqualTo(feeAmountInGbp)) {

                        callbackResponse.addError("The Amount remitted and the amount left to pay must equal the full fee amount");
                    } else if (!amountLeftToPayInGbp.isZero()) {
                        callbackResponse.addError("The amount left to pay must be 0");
                    }
                    break;

                case PARTIALLY_APPROVED:
                    if (!amountRemittedInGbp.add(amountLeftToPayInGbp).equals(feeAmountInGbp)) {

                        callbackResponse.addError("The Amount remitted and the amount left to pay must equal the full fee amount");
                    } else if (amountRemittedInGbp.equals(feeAmountInGbp)) {

                        callbackResponse.addError("The Amount remitted cannot be equal to the full fee amount");
                    }

                    break;

                default:
                    break;
            }
        }

        return callbackResponse;
    }
}
