package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class YesOrNoTest {

    @Test
    public void has_correct_values() {
        assertEquals("No", YesOrNo.No.toString());
        assertEquals("Yes", YesOrNo.Yes.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, YesOrNo.values().length);
    }
}
