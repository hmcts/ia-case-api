package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_WITH_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATED_DECISION_HEARING_FEE_OPTION;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeeService;

public class FeesHelper {

    private FeesHelper() {
    }

    public static final Fee findFeeByHearingType(FeeService feeService, AsylumCase asylumCase) {
        Fee fee;
        Optional<String> decisionHearingFeeOption = asylumCase.read(UPDATED_DECISION_HEARING_FEE_OPTION, String.class);
        if (decisionHearingFeeOption.isPresent()) {

            FeeType feeType = decisionHearingFeeOption.get().equals("decisionWithHearing")
                ? FeeType.FEE_WITH_HEARING
                : FeeType.FEE_WITHOUT_HEARING;

            fee = feeService.getFee(feeType);

            if (!isNull(fee)) {
                switch (decisionHearingFeeOption.get()) {
                    case "decisionWithHearing":
                        asylumCase.write(FEE_WITH_HEARING, fee.getAmountAsString());
                        asylumCase.write(PAYMENT_DESCRIPTION, "Appeal determined with a hearing");
                        asylumCase.clear(FEE_WITHOUT_HEARING);
                        return fee;

                    case "decisionWithoutHearing":
                        asylumCase.write(FEE_WITHOUT_HEARING, fee.getAmountAsString());
                        asylumCase.write(PAYMENT_DESCRIPTION, "Appeal determined without a hearing");
                        asylumCase.clear(FEE_WITH_HEARING);
                        return fee;

                    default:
                        break;
                }
            }
        }
        return null;
    }
}
