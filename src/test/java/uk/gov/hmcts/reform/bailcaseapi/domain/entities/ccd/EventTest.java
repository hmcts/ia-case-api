package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EventTest {

    @Test
    void has_correct_values() {
        assertEquals("startApplication", Event.START_APPLICATION.toString());
        assertEquals("endApplication", Event.END_APPLICATION.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
    }

    @Test
    void fail_if_changes_needed_after_modifying_class() {
        assertEquals(3, Event.values().length);
    }
}
