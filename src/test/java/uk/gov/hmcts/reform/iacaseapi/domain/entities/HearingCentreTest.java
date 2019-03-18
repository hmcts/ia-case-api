package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class HearingCentreTest {

    @Test
    public void has_correct_values() {
        assertEquals("manchester", HearingCentre.MANCHESTER.toString());
        assertEquals("taylorHouse", HearingCentre.TAYLOR_HOUSE.toString());
    }

    @Test
    public void can_be_created_from() {
        assertEquals(HearingCentre.from("manchester").get(), HearingCentre.MANCHESTER);
        assertEquals(HearingCentre.from("taylorHouse").get(), HearingCentre.TAYLOR_HOUSE);
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, HearingCentre.values().length);
    }
}
