package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ListingHearingCentreTest {
    @Test
    void has_correct_values() {
        //assert that the values are the same as the ones in the ListingHearingCentre enum
        assertEquals("birmingham", ListingHearingCentre.BIRMINGHAM.toString());
        assertEquals("bradford", ListingHearingCentre.BRADFORD.toString());
        assertEquals("coventry", ListingHearingCentre.COVENTRY.toString());
        assertEquals("glasgowTribunalsCentre", ListingHearingCentre.GLASGOW_TRIBUNALS_CENTRE.toString());
        assertEquals("hattonCross", ListingHearingCentre.HATTON_CROSS.toString());
        assertEquals("manchester", ListingHearingCentre.MANCHESTER.toString());
        assertEquals("newcastle", ListingHearingCentre.NEWCASTLE.toString());
        assertEquals("newport", ListingHearingCentre.NEWPORT.toString());
        assertEquals("nottingham", ListingHearingCentre.NOTTINGHAM.toString());
        assertEquals("taylorHouse", ListingHearingCentre.TAYLOR_HOUSE.toString());
        assertEquals("belfast", ListingHearingCentre.BELFAST.toString());
        assertEquals("harmondsworth", ListingHearingCentre.HARMONDSWORTH.toString());
        assertEquals("hendon", ListingHearingCentre.HENDON.toString());
        assertEquals("yarlsWood", ListingHearingCentre.YARLS_WOOD.toString());
        assertEquals("bradfordKeighley", ListingHearingCentre.BRADFORD_KEIGHLEY.toString());
        assertEquals("mccMinshull", ListingHearingCentre.MCC_MINSHULL.toString());
        assertEquals("mccCrownSquare", ListingHearingCentre.MCC_CROWN_SQUARE.toString());
        assertEquals("manchesterMags", ListingHearingCentre.MANCHESTER_MAGS.toString());
        assertEquals("nthTyneMags", ListingHearingCentre.NTH_TYNE_MAGS.toString());
        assertEquals("leedsMags", ListingHearingCentre.LEEDS_MAGS.toString());
        assertEquals("alloaSherrif", ListingHearingCentre.ALLOA_SHERRIF.toString());
        assertEquals("remoteHearing", ListingHearingCentre.REMOTE_HEARING.toString());
        assertEquals("decisionWithoutHearing", ListingHearingCentre.DECISION_WITHOUT_HEARING.toString());
    }

    @Test
    void has_correct_label() {
        //assert that the labels are the same as the ones in the ListingHearingCentre enum
        assertEquals("Birmingham", ListingHearingCentre.BIRMINGHAM.getLabel());
        assertEquals("Bradford", ListingHearingCentre.BRADFORD.getLabel());
        assertEquals("Coventry Magistrates Court", ListingHearingCentre.COVENTRY.getLabel());
        assertEquals("Glasgow", ListingHearingCentre.GLASGOW_TRIBUNALS_CENTRE.getLabel());
        assertEquals("Hatton Cross", ListingHearingCentre.HATTON_CROSS.getLabel());
        assertEquals("Manchester", ListingHearingCentre.MANCHESTER.getLabel());
        assertEquals("Newcastle Civil & Family Courts and Tribunals Centre", ListingHearingCentre.NEWCASTLE.getLabel());
        assertEquals("Newport", ListingHearingCentre.NEWPORT.getLabel());
        assertEquals("Nottingham Justice Centre", ListingHearingCentre.NOTTINGHAM.getLabel());
        assertEquals("Taylor House", ListingHearingCentre.TAYLOR_HOUSE.getLabel());
        assertEquals("Belfast", ListingHearingCentre.BELFAST.getLabel());
        assertEquals("Harmondsworth", ListingHearingCentre.HARMONDSWORTH.getLabel());
        assertEquals("Hendon", ListingHearingCentre.HENDON.getLabel());
        assertEquals("Yarl's Wood", ListingHearingCentre.YARLS_WOOD.getLabel());
        assertEquals("Bradford & Keighley", ListingHearingCentre.BRADFORD_KEIGHLEY.getLabel());
        assertEquals("MCC Minshull st", ListingHearingCentre.MCC_MINSHULL.getLabel());
        assertEquals("MCC Crown Square", ListingHearingCentre.MCC_CROWN_SQUARE.getLabel());
        assertEquals("Manchester Mags", ListingHearingCentre.MANCHESTER_MAGS.getLabel());
        assertEquals("NTH Tyne Mags", ListingHearingCentre.NTH_TYNE_MAGS.getLabel());
        assertEquals("Leeds Mags", ListingHearingCentre.LEEDS_MAGS.getLabel());
        assertEquals("Alloa Sherrif Court", ListingHearingCentre.ALLOA_SHERRIF.getLabel());
        assertEquals("Remote", ListingHearingCentre.REMOTE_HEARING.getLabel());
        assertEquals("Decision without hearing", ListingHearingCentre.DECISION_WITHOUT_HEARING.getLabel());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(23, ListingHearingCentre.values().length);
    }
}
