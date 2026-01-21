package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DispatchPriorityTest {

    @Test
    void has_correct_case_event_ids() {
        assertEquals("earliest", DispatchPriority.EARLIEST.toString());
        assertEquals("early", DispatchPriority.EARLY.toString());
        assertEquals("late", DispatchPriority.LATE.toString());
        assertEquals("latest", DispatchPriority.LATEST.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(4, DispatchPriority.values().length);
    }
}
