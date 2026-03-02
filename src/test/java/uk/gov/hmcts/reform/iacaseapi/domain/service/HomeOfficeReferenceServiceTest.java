package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import uk.gov.hmcts.reform.iacaseapi.domain.HomeOfficeMissingApplicationException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import java.util.stream.Stream;

class HomeOfficeReferenceServiceTest {

    private AsylumCase asylumCase;
    private AsylumCase asylumCaseWithHomeOfficeData;
    private HomeOfficeReferenceService service;
    private HomeOfficeApi<AsylumCase> homeOfficeApi;

    private Callback<AsylumCase> callback;
    private CaseDetails<AsylumCase> caseDetails;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        asylumCase = Mockito.mock(AsylumCase.class);
        asylumCaseWithHomeOfficeData = Mockito.mock(AsylumCase.class);
        callback = (Callback<AsylumCase>) mock(Callback.class);
        caseDetails = (CaseDetails<AsylumCase>) mock(CaseDetails.class);
        homeOfficeApi = (HomeOfficeApi<AsylumCase>) Mockito.mock(HomeOfficeApi.class);

        service = new HomeOfficeReferenceService(homeOfficeApi);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(homeOfficeApi.midEvent(callback)).thenReturn(asylumCaseWithHomeOfficeData);
    }

    // -------------------------------------------------------------------------
    // Already cached → should NOT call API
    // -------------------------------------------------------------------------

    @Test
    void shouldCallApiAndReturnAppellantsWhenAlreadyPresent() {

        List<HomeOfficeAppellant> appellants =
            List.of(Mockito.mock(HomeOfficeAppellant.class));

        Mockito.when(asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.of(appellants));

        Optional<List<IdValue<HomeOfficeAppellant>>> result =
            service.getHomeOfficeReferenceData("REF", callback);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(appellants, result.get());

        Mockito.verifyNoInteractions(homeOfficeApi);
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
                () -> service.getHomeOfficeReferenceData("REF", callback)
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

        Mockito.when(asylumCaseWithHomeOfficeData.read(AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty());

        Mockito.when(asylumCaseWithHomeOfficeData.read(
            AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_HTTP_STATUS,
            String.class
        )).thenReturn(Optional.of(status));
    }

    @Test
    void shouldReturnEmptyOptionalWhenStatus200ButNoAppellants() {

        Mockito.when(asylumCaseWithHomeOfficeData.read(
            AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS
        )).thenReturn(Optional.empty());

        Mockito.when(asylumCaseWithHomeOfficeData.read(
            AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_HTTP_STATUS,
            String.class
        )).thenReturn(Optional.of("200"));

        Optional<List<IdValue<HomeOfficeAppellant>>> result =
            service.getHomeOfficeReferenceData("REF", callback);

        Assertions.assertTrue(result.isEmpty());

        Mockito.verify(homeOfficeApi)
            .midEvent(callback);
    }

    @Test
    void shouldHandleMissingHttpStatus() {

        Mockito.when(asylumCaseWithHomeOfficeData.read(
            AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS
        )).thenReturn(Optional.empty());

        Mockito.when(asylumCaseWithHomeOfficeData.read(
            AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_HTTP_STATUS,
            String.class
        )).thenReturn(Optional.empty());

        HomeOfficeMissingApplicationException ex =
            Assertions.assertThrows(
                HomeOfficeMissingApplicationException.class,
                () -> service.getHomeOfficeReferenceData("REF", callback)
            );

        Assertions.assertEquals(0, ex.getHttpStatus());
        Assertions.assertTrue(ex.getMessage().contains("could not be found"));
    }

    @Test
    void shouldRaiseEventEvenWhenApiFails() {

        configureErrorStatus("404");

        Assertions.assertThrows(
            HomeOfficeMissingApplicationException.class,
            () -> service.getHomeOfficeReferenceData("REF", callback)
        );

        Mockito.verify(homeOfficeApi)
            .midEvent(callback);
    }

}
