package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import uk.gov.hmcts.reform.iacaseapi.domain.HomeOfficeMissingApplicationException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import java.util.stream.Stream;

class HomeOfficeReferenceServiceTest {

    private CcdDataService ccdDataService;
    private AsylumCase asylumCase;
    private HomeOfficeReferenceService service;

    @BeforeEach
    void setUp() {
        ccdDataService = Mockito.mock(CcdDataService.class);
        asylumCase = Mockito.mock(AsylumCase.class);
        service = new HomeOfficeReferenceService(ccdDataService);
    }

    // -------------------------------------------------------------------------
    // Already cached → should NOT call API
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnExistingAppellantsWithoutCallingApi() {

        List<HomeOfficeAppellant> appellants =
            List.of(Mockito.mock(HomeOfficeAppellant.class));

        Mockito.when(asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.of(appellants));

        Optional<List<HomeOfficeAppellant>> result =
            service.getHomeOfficeReferenceData("REF", 123L, asylumCase);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(appellants, result.get());

        Mockito.verifyNoInteractions(ccdDataService);
    }

    // -------------------------------------------------------------------------
    // HTTP 200 → should return fresh data
    // -------------------------------------------------------------------------

    @Test
    void shouldCallApiAndReturnAppellantsWhenStatus200() {

        List<HomeOfficeAppellant> appellants =
            List.of(Mockito.mock(HomeOfficeAppellant.class));

        Mockito.when(asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(appellants));

        Mockito.when(asylumCase.read(
            AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_HTTP_STATUS,
            String.class
        )).thenReturn(Optional.of("200"));

        Optional<List<HomeOfficeAppellant>> result =
            service.getHomeOfficeReferenceData("REF", 123L, asylumCase);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(appellants, result.get());

        Mockito.verify(ccdDataService)
            .raiseEvent(123L, Event.GET_HOME_OFFICE_APPELLANT_DATA);
    }

    // -------------------------------------------------------------------------
    // Error scenarios
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("errorStatuses")
    void shouldThrowErrors(String status, int expectedStatus, String expectedMessagePart) {

        configureErrorStatus(status);

        HomeOfficeMissingApplicationException ex =
            Assertions.assertThrows(
                HomeOfficeMissingApplicationException.class,
                () -> service.getHomeOfficeReferenceData("REF", 123L, asylumCase)
            );

        Assertions.assertEquals(expectedStatus, ex.getHttpStatus());
        Assertions.assertTrue(ex.getMessage().contains(expectedMessagePart));
    }

    private static Stream<Arguments> errorStatuses() {
        return Stream.of(
            Arguments.of("400", 400, "not correctly formed"),
            Arguments.of("401", 401, "could not be authenticated"),
            Arguments.of("403", 403, "not authorised"),
            Arguments.of("500", 500, "not available"),
            Arguments.of("501", 501, "not available"),
            Arguments.of("502", 502, "not available"),
            Arguments.of("503", 503, "not available"),
            Arguments.of("504", 504, "not available"),
            Arguments.of("-1", -1, "did not respond"),
            Arguments.of("0", 0, "could not be found"),
            Arguments.of("ABC", 0, "The response from the Home Office validation API could not be found."),
            Arguments.of("999", 999, "HTTP status code was 999"),
            Arguments.of("777", 777, "HTTP status code was 777")
        );
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private void configureErrorStatus(String status) {

        Mockito.when(asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty());

        Mockito.when(asylumCase.read(
            AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_HTTP_STATUS,
            String.class
        )).thenReturn(Optional.of(status));
    }

    @Test
    void shouldReturnEmptyOptionalWhenStatus200ButNoAppellants() {

        Mockito.when(asylumCase.read(
            AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS
        )).thenReturn(Optional.empty());

        Mockito.when(asylumCase.read(
            AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_HTTP_STATUS,
            String.class
        )).thenReturn(Optional.of("200"));

        Optional<List<HomeOfficeAppellant>> result =
            service.getHomeOfficeReferenceData("REF", 123L, asylumCase);

        Assertions.assertTrue(result.isEmpty());

        Mockito.verify(ccdDataService)
            .raiseEvent(123L, Event.GET_HOME_OFFICE_APPELLANT_DATA);
    }

    @Test
    void shouldHandleMissingHttpStatus() {

        Mockito.when(asylumCase.read(
            AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS
        )).thenReturn(Optional.empty());

        Mockito.when(asylumCase.read(
            AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_HTTP_STATUS,
            String.class
        )).thenReturn(Optional.empty());

        HomeOfficeMissingApplicationException ex =
            Assertions.assertThrows(
                HomeOfficeMissingApplicationException.class,
                () -> service.getHomeOfficeReferenceData("REF", 123L, asylumCase)
            );

        Assertions.assertEquals(0, ex.getHttpStatus());
        Assertions.assertTrue(ex.getMessage().contains("could not be found"));
    }

    @Test
    void shouldRaiseEventEvenWhenApiFails() {

        configureErrorStatus("404");

        Assertions.assertThrows(
            HomeOfficeMissingApplicationException.class,
            () -> service.getHomeOfficeReferenceData("REF", 456L, asylumCase)
        );

        Mockito.verify(ccdDataService)
            .raiseEvent(456L, Event.GET_HOME_OFFICE_APPELLANT_DATA);
    }

}
