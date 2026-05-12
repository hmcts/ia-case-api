package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
        assertEquals("hendon", HearingCentre.HENDON.toString());
        assertEquals("yarlsWood", HearingCentre.YARLS_WOOD.toString());
        assertEquals("bradfordKeighley", HearingCentre.BRADFORD_KEIGHLEY.toString());
        assertEquals("mccMinshull", HearingCentre.MCC_MINSHULL.toString());
        assertEquals("mccCrownSquare", HearingCentre.MCC_CROWN_SQUARE.toString());
        assertEquals("manchesterMags", HearingCentre.MANCHESTER_MAGS.toString());
        assertEquals("nthTyneMags", HearingCentre.NTH_TYNE_MAGS.toString());
        assertEquals("leedsMags", HearingCentre.LEEDS_MAGS.toString());
        assertEquals("alloaSherrif", HearingCentre.ALLOA_SHERRIF.toString());
        assertEquals("arnhemHouse", HearingCentre.ARNHEM_HOUSE.toString());
        assertEquals("crownHouse", HearingCentre.CROWN_HOUSE.toString());
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
        assertEquals(HearingCentre.HENDON, HearingCentre.from("hendon").get());
        assertEquals(HearingCentre.YARLS_WOOD, HearingCentre.from("yarlsWood").get());
        assertEquals(HearingCentre.BRADFORD_KEIGHLEY, HearingCentre.from("bradfordKeighley").get());
        assertEquals(HearingCentre.MCC_MINSHULL, HearingCentre.from("mccMinshull").get());
        assertEquals(HearingCentre.MCC_CROWN_SQUARE, HearingCentre.from("mccCrownSquare").get());
        assertEquals(HearingCentre.MANCHESTER_MAGS, HearingCentre.from("manchesterMags").get());
        assertEquals(HearingCentre.NTH_TYNE_MAGS, HearingCentre.from("nthTyneMags").get());
        assertEquals(HearingCentre.LEEDS_MAGS, HearingCentre.from("leedsMags").get());
        assertEquals(HearingCentre.ALLOA_SHERRIF, HearingCentre.from("alloaSherrif").get());
        assertEquals(HearingCentre.ARNHEM_HOUSE, HearingCentre.from("arnhemHouse").get());
        assertEquals(HearingCentre.CROWN_HOUSE, HearingCentre.from("crownHouse").get());
        assertEquals(HearingCentre.REMOTE_HEARING, HearingCentre.from("remoteHearing").get());
        assertEquals(HearingCentre.DECISION_WITHOUT_HEARING, HearingCentre.from("decisionWithoutHearing").get());
    }

    @ParameterizedTest
    @CsvSource({
        "birmingham, 231596",
        "bradford, 698118",
        "coventry, 787030",
        "glasgowTribunalsCentre, 366559",
        "glasgow, 366559",
        "hattonCross, 386417",
        "manchester, 512401",
        "newcastle, 366796",
        "newport, 227101",
        "nottingham, 618632",
        "taylorHouse, 765324",
        "belfast, 999973",
        "harmondsworth, 28837",
        "hendon, 745389",
        "yarlsWood, 649000",
        "bradfordKeighley, 580554",
        "mccMinshull, 326944",
        "mccCrownSquare, 144641",
        "manchesterMags, 783803",
        "nthTyneMags, 443257",
        "leedsMags, 569737",
        "alloaSherrif, 999971",
        "arnhemHouse, 324339",
        "crownHouse, 420587"
    })
    void should_return_epims_id_or_value_by_using_mapping_list(String value, String epimsId) {
        assertEquals(epimsId, HearingCentre.getEpimsIdByValue(value));

        boolean isGlasgowListCaseHearingCentre =
            Objects.equals(HearingCentre.GLASGOW_TRIBUNALS_CENTRE.getValue(), value);
        String actualValue = HearingCentre.fromEpimsId(epimsId, isGlasgowListCaseHearingCentre)
            .map(HearingCentre::getValue).orElse(null);
        assertEquals(value, actualValue);
    }

    @Test
    void fromEpisId_should_return_empty_optional() {

        assertTrue(HearingCentre.fromEpimsId("1111111111111111", false).isEmpty());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(28, HearingCentre.values().length);
    }
}
