package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class HearingCentreTest {

    @Test
    public void has_correct_values() {
        assertEquals("birmingham", HearingCentre.BIRMINGHAM.toString());
        assertEquals("bradford", HearingCentre.BRADFORD.toString());
        assertEquals("coventry", HearingCentre.COVENTRY.toString());
        assertEquals("glasgow", HearingCentre.GLASGOW.toString());
        assertEquals("glasgowTribunalsCentre", HearingCentre.GLASGOW_TRIBUNAL_CENTRE.toString());
        assertEquals("hattonCross", HearingCentre.HATTON_CROSS.toString());
        assertEquals("manchester", HearingCentre.MANCHESTER.toString());
        assertEquals("newport", HearingCentre.NEWPORT.toString());
        assertEquals("northShields", HearingCentre.NORTH_SHIELDS.toString());
        assertEquals("nottingham", HearingCentre.NOTTINGHAM.toString());
        assertEquals("taylorHouse", HearingCentre.TAYLOR_HOUSE.toString());
        assertEquals("newcastle", HearingCentre.NEWCASTLE.toString());
    }

    @Test
    public void can_be_created_from() {
        assertEquals(HearingCentre.from("bradford").get(), HearingCentre.BRADFORD);
        assertEquals(HearingCentre.from("birmingham").get(), HearingCentre.BIRMINGHAM);
        assertEquals(HearingCentre.from("coventry").get(), HearingCentre.COVENTRY);
        assertEquals(HearingCentre.from("glasgow").get(), HearingCentre.GLASGOW);
        assertEquals(HearingCentre.from("glasgowTribunalsCentre").get(), HearingCentre.GLASGOW_TRIBUNAL_CENTRE);
        assertEquals(HearingCentre.from("hattonCross").get(), HearingCentre.HATTON_CROSS);
        assertEquals(HearingCentre.from("manchester").get(), HearingCentre.MANCHESTER);
        assertEquals(HearingCentre.from("northShields").get(), HearingCentre.NORTH_SHIELDS);
        assertEquals(HearingCentre.from("newport").get(), HearingCentre.NEWPORT);
        assertEquals(HearingCentre.from("nottingham").get(), HearingCentre.NOTTINGHAM);
        assertEquals(HearingCentre.from("taylorHouse").get(), HearingCentre.TAYLOR_HOUSE);
        assertEquals(HearingCentre.from("newcastle").get(), HearingCentre.NEWCASTLE);
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(12, HearingCentre.values().length);
    }
}
