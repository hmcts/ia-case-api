package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.elasticsearch.core.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HomeOfficeStatutoryTimeframeDtoTest {

    private String hmctsReferenceNumber;
    private String uan;
    private String familyName;
    private String givenNames;
    private LocalDate dateOfBirth;
    private OffsetDateTime timeStamp;

    private HomeOfficeStatutoryTimeframeDto homeOfficeStatutoryTimeframeDto;

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

        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort cohort = 
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                .name("HU")
                .included("true")
                .build();

        homeOfficeStatutoryTimeframeDto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber(hmctsReferenceNumber)
            .uan(uan)
            .familyName(familyName)
            .givenNames(givenNames)
            .dateOfBirth(dateOfBirth)
            .stf24WeekCohorts(List.of(cohort))
            .timeStamp(timeStamp)
            .build();

        assertEquals(hmctsReferenceNumber, homeOfficeStatutoryTimeframeDto.getHmctsReferenceNumber());
        assertEquals(uan, homeOfficeStatutoryTimeframeDto.getUan());
        assertEquals(familyName, homeOfficeStatutoryTimeframeDto.getFamilyName());
        assertEquals(givenNames, homeOfficeStatutoryTimeframeDto.getGivenNames());
        assertEquals(dateOfBirth, homeOfficeStatutoryTimeframeDto.getDateOfBirth());
        assertEquals(1, homeOfficeStatutoryTimeframeDto.getStf24WeekCohorts().size());
        assertEquals("HU", homeOfficeStatutoryTimeframeDto.getStf24WeekCohorts().get(0).getName());
        assertTrue(Boolean.parseBoolean(homeOfficeStatutoryTimeframeDto.getStf24WeekCohorts().get(0).getIncluded()));
        assertEquals(timeStamp, homeOfficeStatutoryTimeframeDto.getTimeStamp());
    }

    @Test
    void should_hold_onto_values_multiple_cohorts() {

        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort cohort1 = 
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                .name("HU")
                .included("true")
                .build();

        HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort cohort2 = 
            HomeOfficeStatutoryTimeframeDto.Stf24WeekCohort.builder()
                .name("PA")
                .included("false")
                .build();

        homeOfficeStatutoryTimeframeDto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber(hmctsReferenceNumber)
            .uan(uan)
            .familyName(familyName)
            .givenNames(givenNames)
            .dateOfBirth(dateOfBirth)
            .stf24WeekCohorts(List.of(cohort1, cohort2))
            .timeStamp(timeStamp)
            .build();

        assertEquals(2, homeOfficeStatutoryTimeframeDto.getStf24WeekCohorts().size());

        assertEquals("HU", homeOfficeStatutoryTimeframeDto.getStf24WeekCohorts().get(0).getName());
        assertTrue(Boolean.parseBoolean(homeOfficeStatutoryTimeframeDto.getStf24WeekCohorts().get(0).getIncluded()));

        assertEquals("PA", homeOfficeStatutoryTimeframeDto.getStf24WeekCohorts().get(1).getName());
        assertTrue(!Boolean.parseBoolean(homeOfficeStatutoryTimeframeDto.getStf24WeekCohorts().get(1).getIncluded()));
    }

    @Test
    void should_handle_empty_cohorts_array() {
        homeOfficeStatutoryTimeframeDto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber(hmctsReferenceNumber)
            .uan(uan)
            .familyName(familyName)
            .givenNames(givenNames)
            .dateOfBirth(dateOfBirth)
            .stf24WeekCohorts(List.of())
            .timeStamp(timeStamp)
            .build();

        assertEquals(0, homeOfficeStatutoryTimeframeDto.getStf24WeekCohorts().size());
    }

    @Test
    void should_toString_not_throw() {
        homeOfficeStatutoryTimeframeDto = HomeOfficeStatutoryTimeframeDto.builder()
            .hmctsReferenceNumber(hmctsReferenceNumber)
            .uan(uan)
            .familyName(familyName)
            .givenNames(givenNames)
            .dateOfBirth(dateOfBirth)
            .stf24WeekCohorts(List.of())
            .timeStamp(timeStamp)
            .build();

        assertTrue(homeOfficeStatutoryTimeframeDto.toString().contains(hmctsReferenceNumber));
    }

}