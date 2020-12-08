package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;

@Component
public class AppealPaymentConfirmationProvider {

    public String getPaymentReferenceNumber(AsylumCase asylumCase) {
        return asylumCase
            .read(PAYMENT_REFERENCE, String.class)
            .orElse("");
    }

    public String getPaymentAccountNumber(AsylumCase asylumCase) {
        return asylumCase
            .read(PBA_NUMBER, String.class)
            .orElse("");
    }

    public String getFee(AsylumCase asylumCase) {
        String decisionHearingFeeOption = asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)
            .orElse("");

        return decisionHearingFeeOption.equals("decisionWithHearing")
            ? asylumCase.read(FEE_WITH_HEARING, String.class).orElse("")
            : asylumCase.read(FEE_WITHOUT_HEARING, String.class).orElse("");
    }

    public Optional<PaymentStatus> getPaymentStatus(AsylumCase asylumCase) {
        return asylumCase
            .read(PAYMENT_STATUS, PaymentStatus.class);
    }
}
