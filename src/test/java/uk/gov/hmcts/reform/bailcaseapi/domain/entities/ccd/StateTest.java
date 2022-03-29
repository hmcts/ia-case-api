package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StateTest {

    @Test
    void has_correct_values() {
        assertEquals("applicationStarted", State.APPLICATION_STARTED.toString());
        assertEquals("applicationEnded", State.APPLICATION_ENDED.toString());
        assertEquals("applicationSubmitted", State.APPLICATION_SUBMITTED.toString());
        assertEquals("unknown", State.UNKNOWN.toString());
    }

    @Test
    void fail_if_changes_needed_after_modifying_class() {
        assertEquals(4, State.values().length);
    }

}
