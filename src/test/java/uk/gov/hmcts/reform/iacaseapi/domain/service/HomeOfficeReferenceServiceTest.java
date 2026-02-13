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

    @Test
    void shouldThrowExceptionFor404() {

        configureErrorStatus("404");

        HomeOfficeMissingApplicationException ex =
            Assertions.assertThrows(
                HomeOfficeMissingApplicationException.class,
                () -> service.getHomeOfficeReferenceData("REF", 123L, asylumCase)
            );

        Assertions.assertEquals(404, ex.getHttpStatus());
        Assertions.assertTrue(ex.getMessage().contains("No application matching this HMCTS reference number was found."));
    }

    @Test
    void shouldThrowExceptionFor400() {

        configureErrorStatus("400");

        HomeOfficeMissingApplicationException ex =
            Assertions.assertThrows(
                HomeOfficeMissingApplicationException.class,
                () -> service.getHomeOfficeReferenceData("REF", 123L, asylumCase)
            );

        Assertions.assertEquals(400, ex.getHttpStatus());
        Assertions.assertTrue(ex.getMessage().contains("not correctly formed"));
    }

    @Test
    void shouldThrowExceptionFor401() {

        configureErrorStatus("401");

        HomeOfficeMissingApplicationException ex =
            Assertions.assertThrows(
                HomeOfficeMissingApplicationException.class,
                () -> service.getHomeOfficeReferenceData("REF", 123L, asylumCase)
            );

        Assertions.assertEquals(401, ex.getHttpStatus());
        Assertions.assertTrue(ex.getMessage().contains("could not be authenticated"));
    }

    @Test
    void shouldThrowExceptionFor403() {

        configureErrorStatus("403");

        HomeOfficeMissingApplicationException ex =
            Assertions.assertThrows(
                HomeOfficeMissingApplicationException.class,
                () -> service.getHomeOfficeReferenceData("REF", 123L, asylumCase)
            );

        Assertions.assertEquals(403, ex.getHttpStatus());
        Assertions.assertTrue(ex.getMessage().contains("not authorised"));
    }

    @Test
    void shouldThrowExceptionForServerErrors() {

        configureErrorStatus("500");

        HomeOfficeMissingApplicationException ex =
            Assertions.assertThrows(
                HomeOfficeMissingApplicationException.class,
                () -> service.getHomeOfficeReferenceData("REF", 123L, asylumCase)
            );

        Assertions.assertEquals(500, ex.getHttpStatus());
        Assertions.assertTrue(ex.getMessage().contains("not available"));
    }

    @Test
    void shouldThrowExceptionForTimeoutMinusOne() {

        configureErrorStatus("-1");

        HomeOfficeMissingApplicationException ex =
            Assertions.assertThrows(
                HomeOfficeMissingApplicationException.class,
                () -> service.getHomeOfficeReferenceData("REF", 123L, asylumCase)
            );

        Assertions.assertEquals(-1, ex.getHttpStatus());
        Assertions.assertTrue(ex.getMessage().contains("did not respond"));
    }

    @Test
    void shouldThrowExceptionForZeroStatus() {

        configureErrorStatus("0");

        HomeOfficeMissingApplicationException ex =
            Assertions.assertThrows(
                HomeOfficeMissingApplicationException.class,
                () -> service.getHomeOfficeReferenceData("REF", 123L, asylumCase)
            );

        Assertions.assertEquals(0, ex.getHttpStatus());
        Assertions.assertTrue(ex.getMessage().contains("could not be found"));
    }

    @Test
    void shouldHandleNonNumericStatusCode() {

        configureErrorStatus("ABC");

        HomeOfficeMissingApplicationException ex =
            Assertions.assertThrows(
                HomeOfficeMissingApplicationException.class,
                () -> service.getHomeOfficeReferenceData("REF", 123L, asylumCase)
            );

        Assertions.assertEquals(0, ex.getHttpStatus());
        Assertions.assertTrue(
            ex.getMessage().contains(
                "The response from the Home Office validation API could not be found."
            )
        );
    }

    @Test
    void shouldHandleUnknownStatusCode() {

        configureErrorStatus("999");

        HomeOfficeMissingApplicationException ex =
            Assertions.assertThrows(
                HomeOfficeMissingApplicationException.class,
                () -> service.getHomeOfficeReferenceData("REF", 123L, asylumCase)
            );

        Assertions.assertEquals(999, ex.getHttpStatus());
        Assertions.assertTrue(ex.getMessage().contains("HTTP status code was 999"));
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
}
