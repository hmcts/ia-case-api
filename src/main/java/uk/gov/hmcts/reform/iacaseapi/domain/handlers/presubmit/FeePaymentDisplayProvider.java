package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;

@Component
public class FeePaymentDisplayProvider {

    void writeDecisionHearingOptionToCaseData(AsylumCase asylumCase) {

        String hearingFeeOption = asylumCase
            .read(DECISION_HEARING_FEE_OPTION, String.class).orElse("");

        if (hearingFeeOption.equals(DECISION_WITH_HEARING.value())) {

            asylumCase.write(
                HEARING_DECISION_SELECTED,
                "Decision with a hearing. The fee for this type of appeal is £140"
            );

        } else if (hearingFeeOption.equals(DECISION_WITHOUT_HEARING.value())) {

            asylumCase.write(
                HEARING_DECISION_SELECTED,
                "Decision without a hearing. The fee for this type of appeal is £80"
            );
        }
    }
}

