package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class StrategicCaseFlagTest {

    private final String partyName = "some-partyName";
    private final String roleOnCase = "some-roleOnCase";
    @Mock
    private List<CaseFlagDetail> details;

    private StrategicCaseFlag strategicCaseFlag;

    @BeforeEach
    public void setUp() {
        strategicCaseFlag = new StrategicCaseFlag(partyName, roleOnCase, details);
    }

    @Test
    void should_hold_onto_values() {
        assertThat(strategicCaseFlag.getPartyName()).isEqualTo(partyName);
        assertThat(strategicCaseFlag.getRoleOnCase()).isEqualTo(roleOnCase);
        assertThat(strategicCaseFlag.getDetails()).isEqualTo(details);
    }
}