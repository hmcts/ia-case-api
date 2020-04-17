package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.Assert.*;

import org.junit.Test;

public class FeeTypeTest {

    @Test
    public void shoud_have_correct_values() {

        assertEquals("oralFee", FeeType.ORAL_FEE.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(1, FeeType.values().length);
    }
}
