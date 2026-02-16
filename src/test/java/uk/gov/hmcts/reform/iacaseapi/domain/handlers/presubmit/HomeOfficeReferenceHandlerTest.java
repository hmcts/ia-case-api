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

import org.mockito.ArgumentMatchers;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;


class HomeOfficeReferenceHandlerTest {

    private HomeOfficeReferenceService homeOfficeReferenceService;
    private HomeOfficeReferenceHandler handler;
    private AsylumCase asylumCase;

    private Callback<AsylumCase> buildValidCallbackForHomeOfficeRefNo(String reference) {

        @SuppressWarnings("unchecked")
        Callback<AsylumCase> callback = (Callback<AsylumCase>) Mockito.mock(Callback.class);
        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

        @SuppressWarnings("unchecked")
        CaseDetails<AsylumCase> caseDetails = (CaseDetails<AsylumCase>) Mockito.mock(CaseDetails.class);
        Mockito.when(callback.getCaseDetails()).thenReturn(caseDetails);
        Mockito.when(caseDetails.getCaseData()).thenReturn(asylumCase);
        Mockito.when(caseDetails.getId()).thenReturn(123L);

        Mockito.when(asylumCase.read(
            uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER,
            String.class
        )).thenReturn(Optional.of(reference));

        return callback;
    }

    private Callback<AsylumCase> buildValidCallbackForAppellantBasicDetails(String reference) {

        @SuppressWarnings("unchecked")
        Callback<AsylumCase> callback = (Callback<AsylumCase>) Mockito.mock(Callback.class);
        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("appellantBasicDetails");

        @SuppressWarnings("unchecked")
        CaseDetails<AsylumCase> caseDetails = (CaseDetails<AsylumCase>) Mockito.mock(CaseDetails.class);
        Mockito.when(callback.getCaseDetails()).thenReturn(caseDetails);
        Mockito.when(caseDetails.getCaseData()).thenReturn(asylumCase);
        Mockito.when(caseDetails.getId()).thenReturn(123L);

        Mockito.when(asylumCase.read(
            uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER,
            String.class
        )).thenReturn(Optional.of(reference));

        return callback;
    }


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

    @ParameterizedTest
    @MethodSource("caseNumberExceptionStatuses")
    void shouldReturnFalseForAllExceptionStatusesInCaseNumberLookup(int httpStatus) {

        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyLong(),
            ArgumentMatchers.any(AsylumCase.class)
        )).thenThrow(new HomeOfficeMissingApplicationException(httpStatus, "error"));

        boolean result = handler.isMatchingHomeOfficeCaseNumber("REF", 123L, asylumCase);

        Assertions.assertFalse(result);
    }

    private static Stream<Arguments> caseNumberExceptionStatuses() {
        return Stream.of(
            Arguments.of(-1),
            Arguments.of(0),
            Arguments.of(400),
            Arguments.of(401),
            Arguments.of(403),
            Arguments.of(404),
            Arguments.of(500),
            Arguments.of(501),
            Arguments.of(502),
            Arguments.of(503),
            Arguments.of(504),
            Arguments.of(999) // default branch
        );
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

    @Test
    void canHandleShouldReturnTrueForValidCombination() {

        @SuppressWarnings("unchecked")
        Callback<AsylumCase> callback = (Callback<AsylumCase>) Mockito.mock(Callback.class);

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

        boolean result = handler.canHandle(
            PreSubmitCallbackStage.MID_EVENT,
            callback
        );

        Assertions.assertTrue(result);
    }

    @Test
    void canHandleShouldReturnFalseForWrongStage() {

        @SuppressWarnings("unchecked")
        Callback<AsylumCase> callback = (Callback<AsylumCase>) Mockito.mock(Callback.class);

        boolean result = handler.canHandle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        Assertions.assertFalse(result);
    }

    @Test
    void canHandleShouldThrowIfStageNull() {

        @SuppressWarnings("unchecked")
        Callback<AsylumCase> callback = (Callback<AsylumCase>) Mockito.mock(Callback.class);

        Assertions.assertThrows(
            NullPointerException.class,
            () -> handler.canHandle(null, callback)
        );
    }

    @Test
    void handleShouldThrowIfCannotHandle() {

        @SuppressWarnings("unchecked")
        Callback<AsylumCase> callback = (Callback<AsylumCase>) Mockito.mock(Callback.class);

        Assertions.assertThrows(
            IllegalStateException.class,
            () -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)
        );
    }

    @Test
    void shouldReturnErrorWhenReferenceFormatInvalid() {

        Callback<AsylumCase> callback = buildValidCallbackForHomeOfficeRefNo("INVALID");

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        Assertions.assertFalse(response.getErrors().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("homeOfficeExceptionRefNoScenarios")
    void shouldReturnCorrectErrorMessageForAllHomeOfficeRefNoExceptionStatuses(
        int httpStatus,
        String expectedMessage
    ) {

        String reference = "1234-5678-9012-3456";
        Callback<AsylumCase> callback = buildValidCallbackForHomeOfficeRefNo(reference);

        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyLong(),
            ArgumentMatchers.any(AsylumCase.class)
        )).thenThrow(new HomeOfficeMissingApplicationException(httpStatus, "error"));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        Assertions.assertEquals(1, response.getErrors().size());
        Assertions.assertTrue(response.getErrors().contains(expectedMessage));
    }

    private static Stream<Arguments> homeOfficeExceptionRefNoScenarios() {

        String reference = "1234-5678-9012-3456";

        String clientError =
            "An error occurred.  Please report this to HMCTS.";

        String serverError =
            "An error occurred.  Please try again in 15-20 minutes.  If it occurs again, please report this to HMCTS.";

        String caseNotFound =
            "The Home Office reference number " + reference +
            " does not match any existing case records in Home Office systems.  Please check your decision letter and try again.";

        return Stream.of(
            // --- SERVER_ERROR ---
            Arguments.of(-1, serverError),
            Arguments.of(500, serverError),
            Arguments.of(501, serverError),
            Arguments.of(502, serverError),
            Arguments.of(503, serverError),
            Arguments.of(504, serverError),

            // --- CLIENT_ERROR ---
            Arguments.of(0, clientError),
            Arguments.of(400, clientError),
            Arguments.of(401, clientError),
            Arguments.of(403, clientError),

            // --- CASE_NOT_FOUND ---
            Arguments.of(404, caseNotFound),

            // --- DEFAULT branch ---
            Arguments.of(999, clientError)
        );
    }

    @ParameterizedTest
    @MethodSource("homeOfficeExceptionAppellantDetailsScenarios")
    void shouldReturnCorrectErrorMessageForAllHomeOfficeAppellantDetailsExceptionStatuses(
        int httpStatus,
        String expectedMessage
    ) {

        String reference = "1234-5678-9012-3456";
        Callback<AsylumCase> callback = buildValidCallbackForAppellantBasicDetails(reference);

        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyLong(),
            ArgumentMatchers.any(AsylumCase.class)
        )).thenThrow(new HomeOfficeMissingApplicationException(httpStatus, "error"));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        Assertions.assertEquals(1, response.getErrors().size());
        Assertions.assertTrue(response.getErrors().contains(expectedMessage));
    }

    private static Stream<Arguments> homeOfficeExceptionAppellantDetailsScenarios() {

        String reference = "1234-5678-9012-3456";

        String clientError =
            "An error occurred.  Please report this to HMCTS.";

        String serverError =
            "An error occurred.  Please try again in 15-20 minutes.  If it occurs again, please report this to HMCTS.";

        String caseNotFound =
            "The Home Office reference number " + reference +
            " does not match any existing case records in Home Office systems.  Please check your decision letter and try again.";

        return Stream.of(
            // --- SERVER_ERROR ---
            Arguments.of(-1, serverError),
            Arguments.of(500, serverError),
            Arguments.of(501, serverError),
            Arguments.of(502, serverError),
            Arguments.of(503, serverError),
            Arguments.of(504, serverError),

            // --- CLIENT_ERROR ---
            Arguments.of(0, clientError),
            Arguments.of(400, clientError),
            Arguments.of(401, clientError),
            Arguments.of(403, clientError),

            // --- CASE_NOT_FOUND ---
            Arguments.of(404, caseNotFound),

            // --- DEFAULT branch ---
            Arguments.of(999, clientError)
        );
    }

    @Test
    void shouldReturnErrorWhenAppellandDetailsDoNotMatch() {

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

        String reference = "1234-5678-9012-3456";
        Callback<AsylumCase> callback = buildValidCallbackForAppellantBasicDetails(reference);

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        String expectedMessage = "The details provided do not match those held by the Home Office for reference number " + reference + ".  Please check your decision letter and try again.";    

        Assertions.assertEquals(1, response.getErrors().size());
        Assertions.assertTrue(response.getErrors().contains(expectedMessage));

    }


    @Test
    void matchesNameShouldReturnFalseIfEitherNull() {

        Assertions.assertFalse(
            HomeOfficeReferenceHandler.normalizeName(null).equals("something")
        );
    }

}
