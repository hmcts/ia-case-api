package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class CaseFlagTest {

    private final CaseFlagType caseFlagType = CaseFlagType.ANONYMITY;
    private final String additionalInformation = "some additional information";

    private CaseFlag caseFlag = new CaseFlag(caseFlagType, additionalInformation);

    @Test
    public void should_hold_onto_values() {

        assertEquals(caseFlagType, caseFlag.getCaseFlagType());
        assertEquals(additionalInformation, caseFlag.getCaseFlagAdditionalInformation());
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new CaseFlag(null, additionalInformation))
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new CaseFlag(caseFlagType, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
