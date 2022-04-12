package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HearingCentreTest {

    @Test
    void has_correct_values() {
        assertEquals("Birmingham", HearingCentre.BIRMINGHAM.toString());
        assertEquals("Bradford", HearingCentre.BRADFORD.toString());
        assertEquals("Glasgow", HearingCentre.GLASGOW.toString());
        assertEquals("Hatton Cross", HearingCentre.HATTON_CROSS.toString());
        assertEquals("Manchester", HearingCentre.MANCHESTER.toString());
        assertEquals("Newport", HearingCentre.NEWPORT.toString());
        assertEquals("Taylor House", HearingCentre.TAYLOR_HOUSE.toString());
        assertEquals("Yarlswood", HearingCentre.YARLSWOOD.toString());
    }

    @Test
    void can_be_created_from() {
        assertEquals(HearingCentre.BIRMINGHAM, HearingCentre.from("Birmingham").get());
        assertEquals(HearingCentre.BRADFORD, HearingCentre.from("Bradford").get());
        assertEquals(HearingCentre.GLASGOW, HearingCentre.from("Glasgow").get());
        assertEquals(HearingCentre.HATTON_CROSS, HearingCentre.from("Hatton Cross").get());
        assertEquals(HearingCentre.MANCHESTER, HearingCentre.from("Manchester").get());
        assertEquals(HearingCentre.NEWPORT, HearingCentre.from("Newport").get());
        assertEquals(HearingCentre.TAYLOR_HOUSE, HearingCentre.from("Taylor House").get());
        assertEquals(HearingCentre.YARLSWOOD, HearingCentre.from("Yarlswood").get());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(8, HearingCentre.values().length);
    }

}
