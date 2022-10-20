package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class CaseFlagDetailTest {

    private final String caseFlagId = "123";
    @Mock
    private CaseFlagValue caseFlagValue;

    private CaseFlagDetail caseFlagDetail;

    @BeforeEach
    public void setUp() {
        caseFlagDetail = new CaseFlagDetail(caseFlagId, caseFlagValue);
    }

    @Test
    void should_hold_onto_values() {
        assertThat(caseFlagDetail.getId()).isEqualTo(caseFlagId);
        assertThat(caseFlagDetail.getCaseFlagValue()).isEqualTo(caseFlagValue);
    }

}
