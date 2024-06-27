package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdatedTribunalRulesTest {

    @Test
    void has_correct_values() {
        assertEquals("underRule31", UpdateTribunalRules.UNDER_RULE_31.toString());
        assertEquals("underRule32", UpdateTribunalRules.UNDER_RULE_32.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, UpdateTribunalRules.values().length);
    }
}
