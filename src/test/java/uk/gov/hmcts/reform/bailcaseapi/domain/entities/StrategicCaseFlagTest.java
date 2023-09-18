package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StrategicCaseFlagTest {

    private final String appellantName = "some-appellant-name";

    private StrategicCaseFlag strategicCaseFlag;

    @BeforeEach
    public void setUp() {
        strategicCaseFlag = new StrategicCaseFlag(appellantName);
    }

    @Test
    void should_hold_onto_values() {
        assertThat(strategicCaseFlag.getPartyName()).isEqualTo((appellantName));
        assertThat(strategicCaseFlag.getRoleOnCase()).isEqualTo(("Appellant"));
        assertThat(strategicCaseFlag.getDetails()).isEqualTo((Collections.emptyList()));
    }
}
