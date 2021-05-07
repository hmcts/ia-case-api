package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class OutOfTimeDecisionTypeTest {

    @Test
    void has_correct_values() {

        assertEquals("inTime", OutOfTimeDecisionType.IN_TIME.toString());
        assertEquals("approved", OutOfTimeDecisionType.APPROVED.toString());
        assertEquals("rejected", OutOfTimeDecisionType.REJECTED.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {

        assertEquals(3, OutOfTimeDecisionType.values().length);
    }
}
