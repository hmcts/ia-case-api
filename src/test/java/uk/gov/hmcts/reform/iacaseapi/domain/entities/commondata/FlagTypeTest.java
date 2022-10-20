package uk.gov.hmcts.reform.iacaseapi.domain.entities.commondata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FlagTypeTest {

    @Test
    void has_correct_values() {
        assertEquals("PARTY", FlagType.PARTY.toString());
        assertEquals("CASE", FlagType.CASE.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, FlagType.values().length);
    }
}
