package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import uk.gov.hmcts.reform.iacaseapi.domain.HomeOfficeMissingApplicationException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeReferenceService;

class HomeOfficeReferenceHandlerTest {

    private HomeOfficeReferenceService homeOfficeReferenceService;
    private HomeOfficeReferenceHandler handler;
    private AsylumCase asylumCase;

    @BeforeEach
    void setUp() {
        homeOfficeReferenceService = Mockito.mock(HomeOfficeReferenceService.class);
        asylumCase = Mockito.mock(AsylumCase.class);
        handler = new HomeOfficeReferenceHandler(homeOfficeReferenceService);
    }

    // -------------------------------------------------------------------------
    // isWellFormedHomeOfficeReference
    // -------------------------------------------------------------------------

    @Test
    void shouldValidateUanFormat() {
        Assertions.assertTrue(
            HomeOfficeReferenceHandler.isWellFormedHomeOfficeReference("1234-5678-9012-3456")
        );
    }

    @Test
    void shouldValidateGwfFormat() {
        Assertions.assertTrue(
            HomeOfficeReferenceHandler.isWellFormedHomeOfficeReference("GWF123456789")
        );
    }

    @Test
    void shouldRejectInvalidFormat() {
        Assertions.assertFalse(
            HomeOfficeReferenceHandler.isWellFormedHomeOfficeReference("INVALID")
        );
    }

    @Test
    void shouldRejectNullReference() {
        Assertions.assertFalse(
            HomeOfficeReferenceHandler.isWellFormedHomeOfficeReference(null)
        );
    }

    // -------------------------------------------------------------------------
    // normalizeName
    // -------------------------------------------------------------------------

    @Test
    void shouldNormalizeNameByLowercasingTrimmingAndRemovingAccents() {
        String result = HomeOfficeReferenceHandler.normalizeName("  José   García  ");
        Assertions.assertEquals("jose garcia", result);
    }

    @Test
    void shouldReturnEmptyStringWhenNameIsNull() {
        Assertions.assertEquals("", HomeOfficeReferenceHandler.normalizeName(null));
    }

    // -------------------------------------------------------------------------
    // isMatchingHomeOfficeCaseNumber
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnTrueWhenHomeOfficeCaseNumberMatches() {
        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData(
            Mockito.anyString(),
            Mockito.anyLong(),
            Mockito.any(AsylumCase.class)
        )).thenReturn(Optional.of(List.of(Mockito.mock(HomeOfficeAppellant.class))));

        boolean result = handler.isMatchingHomeOfficeCaseNumber("REF", 123L, asylumCase);

        Assertions.assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenNoHomeOfficeCaseNumberMatch() {
        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData(
            Mockito.anyString(),
            Mockito.anyLong(),
            Mockito.any(AsylumCase.class)
        )).thenReturn(Optional.of(Collections.emptyList()));

        boolean result = handler.isMatchingHomeOfficeCaseNumber("REF", 123L, asylumCase);

        Assertions.assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenReferenceIsNull() {
        boolean result = handler.isMatchingHomeOfficeCaseNumber(null, 123L, asylumCase);
        Assertions.assertFalse(result);
    }

    @Test
    void shouldHandleHomeOfficeMissingApplicationException404() {
        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData(
            Mockito.anyString(),
            Mockito.anyLong(),
            Mockito.any(AsylumCase.class)
        )).thenThrow(new HomeOfficeMissingApplicationException(404, "Not found"));

        boolean result = handler.isMatchingHomeOfficeCaseNumber("REF", 123L, asylumCase);

        Assertions.assertFalse(result);
    }

    @Test
    void shouldHandleHomeOfficeMissingApplicationException500() {
        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData(
            Mockito.anyString(),
            Mockito.anyLong(),
            Mockito.any(AsylumCase.class)
        )).thenThrow(new HomeOfficeMissingApplicationException(500, "Server error"));

        boolean result = handler.isMatchingHomeOfficeCaseNumber("REF", 123L, asylumCase);

        Assertions.assertFalse(result);
    }

    // -------------------------------------------------------------------------
    // isMatchingHomeOfficeCaseDetails
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnTrueWhenDetailsMatch() {

        HomeOfficeAppellant appellant = Mockito.mock(HomeOfficeAppellant.class);

        Mockito.when(appellant.getFamilyName()).thenReturn("Smith");
        Mockito.when(appellant.getGivenNames()).thenReturn("John");
        Mockito.when(appellant.getDateOfBirth()).thenReturn(LocalDate.of(1990, 1, 1));

        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData(
            Mockito.anyString(),
            Mockito.anyLong(),
            Mockito.any(AsylumCase.class)
        )).thenReturn(Optional.of(List.of(appellant)));

        Mockito.when(asylumCase.read(
            uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME,
            String.class
        )).thenReturn(Optional.of("Smith"));

        Mockito.when(asylumCase.read(
            uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES,
            String.class
        )).thenReturn(Optional.of("John"));

        Mockito.when(asylumCase.read(
            uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH,
            String.class
        )).thenReturn(Optional.of("1990-01-01"));

        boolean result = handler.isMatchingHomeOfficeCaseDetails("REF", 123L, asylumCase);

        Assertions.assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenDetailsDoNotMatch() {

        HomeOfficeAppellant appellant = Mockito.mock(HomeOfficeAppellant.class);

        Mockito.when(appellant.getFamilyName()).thenReturn("Smith");
        Mockito.when(appellant.getGivenNames()).thenReturn("John");
        Mockito.when(appellant.getDateOfBirth()).thenReturn(LocalDate.of(1990, 1, 1));

        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData(
            Mockito.anyString(),
            Mockito.anyLong(),
            Mockito.any(AsylumCase.class)
        )).thenReturn(Optional.of(List.of(appellant)));

        Mockito.when(asylumCase.read(
            uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME,
            String.class
        )).thenReturn(Optional.of("Different"));

        Mockito.when(asylumCase.read(
            uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES,
            String.class
        )).thenReturn(Optional.of("Person"));

        Mockito.when(asylumCase.read(
            uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH,
            String.class
        )).thenReturn(Optional.of("2000-01-01"));

        boolean result = handler.isMatchingHomeOfficeCaseDetails("REF", 123L, asylumCase);

        Assertions.assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenHomeOfficeReturnsEmptyOptional() {

        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData(
            Mockito.anyString(),
            Mockito.anyLong(),
            Mockito.any(AsylumCase.class)
        )).thenReturn(Optional.empty());

        boolean result = handler.isMatchingHomeOfficeCaseDetails("REF", 123L, asylumCase);

        Assertions.assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenReferenceIsNullForDetails() {
        boolean result = handler.isMatchingHomeOfficeCaseDetails(null, 123L, asylumCase);
        Assertions.assertFalse(result);
    }

    @Test
    void shouldHandleExceptionDuringDetailsLookup() {

        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData(
            Mockito.anyString(),
            Mockito.anyLong(),
            Mockito.any(AsylumCase.class)
        )).thenThrow(new HomeOfficeMissingApplicationException(400, "Client error"));

        boolean result = handler.isMatchingHomeOfficeCaseDetails("REF", 123L, asylumCase);

        Assertions.assertFalse(result);
    }
}
