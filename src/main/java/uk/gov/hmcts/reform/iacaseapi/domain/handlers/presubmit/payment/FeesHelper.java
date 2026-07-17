package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_CODE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_PAYMENT_APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_VERSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_WITH_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_DESCRIPTION;

import java.math.BigDecimal;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeeService;

public class FeesHelper {

    private static final String DECISION_WITH_HEARING = "decisionWithHearing";
    private static final String DECISION_WITHOUT_HEARING = "decisionWithoutHearing";

    private FeesHelper() {
    }

    public static final Fee findFeeByHearingType(FeeService feeService, AsylumCase asylumCase) {
        Optional<String> decisionHearingFeeOption = asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class);
        if (decisionHearingFeeOption.isPresent()) {

            String hearingOption = decisionHearingFeeOption.get();
            FeeType feeType = DECISION_WITH_HEARING.equals(hearingOption)
                    ? FeeType.FEE_WITH_HEARING
                    : FeeType.FEE_WITHOUT_HEARING;

            Fee fee = feeService.getFee(feeType);

            if (!isNull(fee)) {
                writeFeeDetails(asylumCase, fee);
                writeHearingSpecificDetails(asylumCase, fee, hearingOption);
                return fee;
            }
        }
        return null;
    }

    private static void writeFeeDetails(AsylumCase asylumCase, Fee fee) {
        String feeAmountInPence =
                String.valueOf(new BigDecimal(fee.getAmountAsString()).multiply(new BigDecimal("100")));
        asylumCase.write(FEE_CODE, fee.getCode());
        asylumCase.write(FEE_DESCRIPTION, fee.getDescription());
        asylumCase.write(FEE_VERSION, fee.getVersion());
        asylumCase.write(FEE_AMOUNT_GBP, feeAmountInPence);
        asylumCase.write(FEE_PAYMENT_APPEAL_TYPE, YesOrNo.YES);
    }

    private static void writeHearingSpecificDetails(AsylumCase asylumCase, Fee fee, String hearingOption) {
        if (DECISION_WITH_HEARING.equals(hearingOption)) {
            asylumCase.write(FEE_WITH_HEARING, fee.getAmountAsString());
            asylumCase.write(PAYMENT_DESCRIPTION, "Appeal determined with a hearing");
        } else if (DECISION_WITHOUT_HEARING.equals(hearingOption)) {
            asylumCase.write(FEE_WITHOUT_HEARING, fee.getAmountAsString());
            asylumCase.write(PAYMENT_DESCRIPTION, "Appeal determined without a hearing");
        }
    }
}
