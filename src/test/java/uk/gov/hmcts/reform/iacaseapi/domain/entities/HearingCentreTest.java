package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HearingCentreTest {

    @Test
    void has_correct_values() {
        assertEquals("birmingham", HearingCentre.BIRMINGHAM.toString());
        assertEquals("bradford", HearingCentre.BRADFORD.toString());
        assertEquals("coventry", HearingCentre.COVENTRY.toString());
        assertEquals("glasgow", HearingCentre.GLASGOW.toString());
        assertEquals("glasgowTribunalsCentre", HearingCentre.GLASGOW_TRIBUNALS_CENTRE.toString());
        assertEquals("hattonCross", HearingCentre.HATTON_CROSS.toString());
        assertEquals("manchester", HearingCentre.MANCHESTER.toString());
        assertEquals("newport", HearingCentre.NEWPORT.toString());
        assertEquals("northShields", HearingCentre.NORTH_SHIELDS.toString());
        assertEquals("nottingham", HearingCentre.NOTTINGHAM.toString());
        assertEquals("taylorHouse", HearingCentre.TAYLOR_HOUSE.toString());
        assertEquals("newcastle", HearingCentre.NEWCASTLE.toString());
        assertEquals("belfast", HearingCentre.BELFAST.toString());
        assertEquals("harmondsworth", HearingCentre.HARMONDSWORTH.toString());
        assertEquals("yarlswood", HearingCentre.YARLSWOOD.toString());
        assertEquals("remoteHearing", HearingCentre.REMOTE_HEARING.toString());
        assertEquals("decisionWithoutHearing", HearingCentre.DECISION_WITHOUT_HEARING.toString());
    }

    @Test
    void can_be_created_from() {
        assertEquals(HearingCentre.BRADFORD, HearingCentre.from("bradford").get());
        assertEquals(HearingCentre.BIRMINGHAM, HearingCentre.from("birmingham").get());
        assertEquals(HearingCentre.COVENTRY, HearingCentre.from("coventry").get());
        assertEquals(HearingCentre.GLASGOW, HearingCentre.from("glasgow").get());
        assertEquals(HearingCentre.GLASGOW_TRIBUNALS_CENTRE, HearingCentre.from("glasgowTribunalsCentre").get());
        assertEquals(HearingCentre.HATTON_CROSS, HearingCentre.from("hattonCross").get());
        assertEquals(HearingCentre.MANCHESTER, HearingCentre.from("manchester").get());
        assertEquals(HearingCentre.NORTH_SHIELDS, HearingCentre.from("northShields").get());
        assertEquals(HearingCentre.NEWPORT, HearingCentre.from("newport").get());
        assertEquals(HearingCentre.NOTTINGHAM, HearingCentre.from("nottingham").get());
        assertEquals(HearingCentre.TAYLOR_HOUSE, HearingCentre.from("taylorHouse").get());
        assertEquals(HearingCentre.NEWCASTLE, HearingCentre.from("newcastle").get());
        assertEquals(HearingCentre.BELFAST, HearingCentre.from("belfast").get());
        assertEquals(HearingCentre.HARMONDSWORTH, HearingCentre.from("harmondsworth").get());
        assertEquals(HearingCentre.YARLSWOOD, HearingCentre.from("yarlswood").get());
        assertEquals(HearingCentre.REMOTE_HEARING, HearingCentre.from("remoteHearing").get());
        assertEquals(HearingCentre.DECISION_WITHOUT_HEARING, HearingCentre.from("decisionWithoutHearing").get());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(17, HearingCentre.values().length);
    }
}
