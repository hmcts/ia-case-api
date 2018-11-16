package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class EventTest {

    @Test
    public void has_correct_case_event_ids() {
        assertEquals("startAppeal", Event.START_APPEAL.toString());
        assertEquals("submitAppeal", Event.SUBMIT_APPEAL.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, Event.values().length);
    }
}
