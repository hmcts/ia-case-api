package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

class HomeOfficeStatutoryTimeframeTest {

    private String hmctsReferenceNumber;
    private String uan;
    private String familyName;
    private String givenNames;
    private LocalDate dateOfBirth;
    private OffsetDateTime timeStamp;

    private HomeOfficeStatutoryTimeframe homeOfficeStatutoryTimeframe;

    @BeforeEach
    void setUp() {
        hmctsReferenceNumber = "PA/12345/2026";
        uan = "1234-5678-9012-3456";
        familyName = "Smith";
        givenNames = "John";
        dateOfBirth = LocalDate.of(1990, 1, 1);
        timeStamp = OffsetDateTime.of(2023, 12, 1, 14, 30, 0, 0, ZoneOffset.UTC);
    }

    @Test
    void should_hold_onto_values_single_cohort() {

        HomeOfficeStatutoryTimeframe.Stf24WeekCohort cohort = 
            HomeOfficeStatutoryTimeframe.Stf24WeekCohort.builder()
                .name("HU")
                .included("true")
                .build();
        IdValue<HomeOfficeStatutoryTimeframe.Stf24WeekCohort> idValCohort = new IdValue<>("1", cohort);
        homeOfficeStatutoryTimeframe = HomeOfficeStatutoryTimeframe.builder()
            .hmctsReferenceNumber(hmctsReferenceNumber)
            .uan(uan)
            .familyName(familyName)
            .givenNames(givenNames)
            .dateOfBirth(dateOfBirth)
            .stf24weekCohorts(List.of(idValCohort))
            .timeStamp(timeStamp)
            .build();

        assertEquals(hmctsReferenceNumber, homeOfficeStatutoryTimeframe.getHmctsReferenceNumber());
        assertEquals(uan, homeOfficeStatutoryTimeframe.getUan());
        assertEquals(familyName, homeOfficeStatutoryTimeframe.getFamilyName());
        assertEquals(givenNames, homeOfficeStatutoryTimeframe.getGivenNames());
        assertEquals(dateOfBirth, homeOfficeStatutoryTimeframe.getDateOfBirth());
        assertEquals(1, homeOfficeStatutoryTimeframe.getStf24weekCohorts().size());
        assertEquals("HU", homeOfficeStatutoryTimeframe.getStf24weekCohorts().get(0).getValue().getName());
        assertTrue(Boolean.parseBoolean(homeOfficeStatutoryTimeframe.getStf24weekCohorts().get(0).getValue().getIncluded()));
        assertEquals(timeStamp, homeOfficeStatutoryTimeframe.getTimeStamp());
    }

    @Test
    void should_hold_onto_values_multiple_cohorts() {

        HomeOfficeStatutoryTimeframe.Stf24WeekCohort cohort1 = 
            HomeOfficeStatutoryTimeframe.Stf24WeekCohort.builder()
                .name("HU")
                .included("true")
                .build();

        HomeOfficeStatutoryTimeframe.Stf24WeekCohort cohort2 = 
            HomeOfficeStatutoryTimeframe.Stf24WeekCohort.builder()
                .name("PA")
                .included("false")
                .build();
        IdValue<HomeOfficeStatutoryTimeframe.Stf24WeekCohort> idValCohort1 = new IdValue<>("1", cohort1);
        IdValue<HomeOfficeStatutoryTimeframe.Stf24WeekCohort> idValCohort2 = new IdValue<>("2", cohort2);
        homeOfficeStatutoryTimeframe = HomeOfficeStatutoryTimeframe.builder()
            .hmctsReferenceNumber(hmctsReferenceNumber)
            .uan(uan)
            .familyName(familyName)
            .givenNames(givenNames)
            .dateOfBirth(dateOfBirth)
            .stf24weekCohorts(List.of(idValCohort1, idValCohort2))
            .timeStamp(timeStamp)
            .build();

        assertEquals(2, homeOfficeStatutoryTimeframe.getStf24weekCohorts().size());

        assertEquals("HU", homeOfficeStatutoryTimeframe.getStf24weekCohorts().get(0).getValue().getName());
        assertTrue(Boolean.parseBoolean(homeOfficeStatutoryTimeframe.getStf24weekCohorts().get(0).getValue().getIncluded()));

        assertEquals("PA", homeOfficeStatutoryTimeframe.getStf24weekCohorts().get(1).getValue().getName());
        assertFalse(Boolean.parseBoolean(homeOfficeStatutoryTimeframe.getStf24weekCohorts().get(1).getValue().getIncluded()));
    }

    @Test
    void should_handle_empty_cohorts_array() {
        homeOfficeStatutoryTimeframe = HomeOfficeStatutoryTimeframe.builder()
            .hmctsReferenceNumber(hmctsReferenceNumber)
            .uan(uan)
            .familyName(familyName)
            .givenNames(givenNames)
            .dateOfBirth(dateOfBirth)
            .stf24weekCohorts(List.of())
            .timeStamp(timeStamp)
            .build();

        assertEquals(0, homeOfficeStatutoryTimeframe.getStf24weekCohorts().size());
    }

    @Test
    void should_allow_null_cohorts_when_not_provided() {
        homeOfficeStatutoryTimeframe = HomeOfficeStatutoryTimeframe.builder()
            .hmctsReferenceNumber(hmctsReferenceNumber)
            .uan(uan)
            .familyName(familyName)
            .givenNames(givenNames)
            .dateOfBirth(dateOfBirth)
            .timeStamp(timeStamp)
            .build();

        assertEquals(null, homeOfficeStatutoryTimeframe.getStf24weekCohorts());
    }

    @Test
    void should_accept_empty_optional_fields() {
        homeOfficeStatutoryTimeframe = HomeOfficeStatutoryTimeframe.builder()
            .hmctsReferenceNumber(hmctsReferenceNumber)
            .uan("")
            .familyName(familyName)
            .givenNames(givenNames)
            .stf24weekCohorts(List.of())
            .timeStamp(timeStamp)
            .build();

        assertEquals("", homeOfficeStatutoryTimeframe.getUan());
    }
}