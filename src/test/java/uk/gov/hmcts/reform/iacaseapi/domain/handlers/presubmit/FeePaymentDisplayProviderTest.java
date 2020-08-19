package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class FeePaymentDisplayProviderTest {

    private FeePaymentDisplayProvider feePaymentDisplayProvider;

    @Before
    public void setUp() {

        feePaymentDisplayProvider = new FeePaymentDisplayProvider();
    }

    @Test
    public void should_write_correct_hearing_option_to_asylum_case_for_decision_with_a_hearing() {

        AsylumCase asylumCase = new AsylumCase();

        asylumCase.write(DECISION_HEARING_FEE_OPTION, "decisionWithHearing");

        feePaymentDisplayProvider.writeDecisionHearingOptionToCaseData(asylumCase);

        assertThat(asylumCase.read(HEARING_DECISION_SELECTED)).isEqualTo(Optional.of("Decision with a hearing. The fee for this type of appeal is £140"));
    }

    @Test
    public void should_write_correct_hearing_option_to_asylum_case_for_decision_without_a_hearing() {

        AsylumCase asylumCase = new AsylumCase();

        asylumCase.write(DECISION_HEARING_FEE_OPTION, "decisionWithoutHearing");

        feePaymentDisplayProvider.writeDecisionHearingOptionToCaseData(asylumCase);

        assertThat(asylumCase.read(HEARING_DECISION_SELECTED)).isEqualTo(Optional.of("Decision without a hearing. The fee for this type of appeal is £80"));
    }
}
