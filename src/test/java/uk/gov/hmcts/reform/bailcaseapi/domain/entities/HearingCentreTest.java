package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HearingCentreTest {

    @Test
    void has_correct_values() {
        assertEquals("birmingham", HearingCentre.BIRMINGHAM.toString());
        assertEquals("bradford", HearingCentre.BRADFORD.toString());
        assertEquals("glasgow", HearingCentre.GLASGOW.toString());
        assertEquals("hattonCross", HearingCentre.HATTON_CROSS.toString());
        assertEquals("manchester", HearingCentre.MANCHESTER.toString());
        assertEquals("newcastle", HearingCentre.NEWCASTLE.toString());
        assertEquals("newport", HearingCentre.NEWPORT.toString());
        assertEquals("taylorHouse", HearingCentre.TAYLOR_HOUSE.toString());
        assertEquals("yarlsWood", HearingCentre.YARLS_WOOD.toString());
    }

    @Test
    void can_be_created_from() {
        assertEquals(HearingCentre.BIRMINGHAM, HearingCentre.from("birmingham").get());
        assertEquals(HearingCentre.BRADFORD, HearingCentre.from("bradford").get());
        assertEquals(HearingCentre.GLASGOW, HearingCentre.from("glasgow").get());
        assertEquals(HearingCentre.HATTON_CROSS, HearingCentre.from("hattonCross").get());
        assertEquals(HearingCentre.MANCHESTER, HearingCentre.from("manchester").get());
        assertEquals(HearingCentre.NEWCASTLE, HearingCentre.from("newcastle").get());
        assertEquals(HearingCentre.NEWPORT, HearingCentre.from("newport").get());
        assertEquals(HearingCentre.TAYLOR_HOUSE, HearingCentre.from("taylorHouse").get());
        assertEquals(HearingCentre.YARLS_WOOD, HearingCentre.from("yarlsWood").get());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(9, HearingCentre.values().length);
    }

}
