package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TtlTest {

    private final String systemTTL = "2022-10-21";
    private final String overrideTTL = "2023-10-21";
    private final YesOrNo suspended = YesOrNo.YES;

    private TTL ttl = TTL.builder()
        .suspended(suspended)
        .overrideTTL(overrideTTL)
        .systemTTL(systemTTL)
        .build();

    @Test
    void should_hold_onto_values() {

        assertEquals(suspended, ttl.getSuspended());
        assertEquals(overrideTTL, ttl.getOverrideTTL());
        assertEquals(systemTTL, ttl.getSystemTTL());
    }
}