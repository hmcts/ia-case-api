package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CaseFlagPathTest {

    private final String caseFlagPathId = "123";
    private final String caseFlagPathValue = "some-value";

    private CaseFlagPath caseFlagPath;

    @BeforeEach
    public void setUp() {
        caseFlagPath = new CaseFlagPath(caseFlagPathId, caseFlagPathValue);
    }

    @Test
    void should_hold_onto_values() {
        assertThat(caseFlagPath.getId()).isEqualTo(caseFlagPathId);
        assertThat(caseFlagPath.getValue()).isEqualTo(caseFlagPathValue);
    }

}