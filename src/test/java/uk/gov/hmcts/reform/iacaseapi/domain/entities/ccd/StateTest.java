package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class StateTest {

    @Test
    public void has_correct_case_state_ids() {
        assertEquals("appealStarted", State.APPEAL_STARTED.toString());
        assertEquals("appealSubmitted", State.APPEAL_SUBMITTED.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, State.values().length);
    }
}
