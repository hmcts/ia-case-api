package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import uk.gov.hmcts.reform.iacaseapi.domain.HomeOfficeMissingApplicationException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeReferenceService;

class HomeOfficeReferenceHandlerTest {

    private HomeOfficeReferenceService homeOfficeReferenceService;
    private HomeOfficeReferenceHandler handler;

    private Callback<AsylumCase> callback;
    private CaseDetails<AsylumCase> caseDetails;
    private AsylumCase asylumCase;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        homeOfficeReferenceService = Mockito.mock(HomeOfficeReferenceService.class);
        handler = new HomeOfficeReferenceHandler(homeOfficeReferenceService);

        callback = Mockito.mock(Callback.class);
        caseDetails = Mockito.mock(CaseDetails.class);
        asylumCase = Mockito.mock(AsylumCase.class);

        Mockito.when(callback.getCaseDetails()).thenReturn(caseDetails);
        Mockito.when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    // ----------------------------------------------------
    // canHandle
    // ----------------------------------------------------

    @Test
    void canHandleReturnsTrueForValidCombination() {
        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

        boolean result = handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback);

        Assertions.assertTrue(result);
    }

    @Test
    void canHandleReturnsFalseForWrongStage() {
        boolean result = handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        Assertions.assertFalse(result);
    }

    @Test
    void canHandleThrowsIfNullStage() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> handler.canHandle(null, callback)
        );
    }

    // ----------------------------------------------------
    // isWellFormedHomeOfficeReference
    // ----------------------------------------------------

    @Test
    void validatesReferenceFormats() {
        Assertions.assertTrue(HomeOfficeReferenceHandler.isWellFormedHomeOfficeReference("1234-5678-9012-3456"));
        Assertions.assertTrue(HomeOfficeReferenceHandler.isWellFormedHomeOfficeReference("GWF123456789"));
        Assertions.assertFalse(HomeOfficeReferenceHandler.isWellFormedHomeOfficeReference("BAD"));
        Assertions.assertFalse(HomeOfficeReferenceHandler.isWellFormedHomeOfficeReference(null));
    }

    // ----------------------------------------------------
    // normalizeName
    // ----------------------------------------------------

    @Test
    void normalizesNamesCorrectly() {
        String result = HomeOfficeReferenceHandler.normalizeName("  José   García ");
        Assertions.assertEquals("jose garcia", result);
    }

    // ----------------------------------------------------
    // isMatchingHomeOfficeCaseNumber
    // ----------------------------------------------------

    @Test
    void returnsTrueWhenCaseNumberMatches() {
        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData("REF", callback))
            .thenReturn(Optional.of(List.of(new IdValue<>("1", Mockito.mock(HomeOfficeAppellant.class)))));

        Assertions.assertTrue(handler.isMatchingHomeOfficeCaseNumber("REF", callback));
    }

    @Test
    void returnsFalseWhenNoMatch() {
        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData("REF", callback))
            .thenReturn(Optional.of(Collections.emptyList()));

        Assertions.assertFalse(handler.isMatchingHomeOfficeCaseNumber("REF", callback));
    }

    @Test
    void returnsFalseWhenExceptionThrown() {
        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData("REF", callback))
            .thenThrow(new HomeOfficeMissingApplicationException(404, "error"));

        Assertions.assertFalse(handler.isMatchingHomeOfficeCaseNumber("REF", callback));
    }

    // ----------------------------------------------------
    // isMatchingHomeOfficeCaseDetails
    // ----------------------------------------------------

    @Test
    void returnsTrueWhenDetailsMatch() {

        HomeOfficeAppellant appellant = Mockito.mock(HomeOfficeAppellant.class);

        Mockito.when(appellant.getFamilyName()).thenReturn("Smith");
        Mockito.when(appellant.getGivenNames()).thenReturn("John");
        Mockito.when(appellant.getDateOfBirth()).thenReturn("1990-01-01");

        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData("REF", callback))
            .thenReturn(Optional.of(List.of(new IdValue<>("1", appellant))));

        Mockito.when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Smith"));

        Mockito.when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("John"));

        Mockito.when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("1990-01-01"));

        Assertions.assertTrue(
            handler.isMatchingHomeOfficeCaseDetails("REF", asylumCase, callback)
        );
    }

    @Test
    void returnsFalseWhenDetailsDoNotMatch() {

        HomeOfficeAppellant appellant = Mockito.mock(HomeOfficeAppellant.class);

        Mockito.when(appellant.getFamilyName()).thenReturn("Smith");
        Mockito.when(appellant.getGivenNames()).thenReturn("John");
        Mockito.when(appellant.getDateOfBirth()).thenReturn("1990-01-01");

        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData("REF", callback))
            .thenReturn(Optional.of(List.of(new IdValue<>("1", appellant))));

        Mockito.when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Different"));

        Mockito.when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("Person"));

        Mockito.when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("2000-01-01"));

        Assertions.assertFalse(
            handler.isMatchingHomeOfficeCaseDetails("REF", asylumCase, callback)
        );
    }

    @Test
    void returnsFalseWhenDetailsLookupThrowsException() {
        Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData("REF", callback))
            .thenThrow(new HomeOfficeMissingApplicationException(500, "error"));

        Assertions.assertFalse(
            handler.isMatchingHomeOfficeCaseDetails("REF", asylumCase, callback)
        );
    }

    // ----------------------------------------------------
    // handle (main integration behaviour)
    // ----------------------------------------------------

    @Test
    void handleReturnsErrorWhenReferenceInvalid() {

        try (MockedStatic<HandlerUtils> utilities = Mockito.mockStatic(HandlerUtils.class)) {

            utilities.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(false);
            utilities.when(() -> HandlerUtils.outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase)).thenReturn(false);

            Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
            Mockito.when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

            Mockito.when(asylumCase.read(
                AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER,
                String.class
            )).thenReturn(Optional.of("BAD"));

            PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

            Assertions.assertFalse(response.getErrors().isEmpty());
        }
    }

    @Test
    void handleReturnsSuccessWhenAllValid() {

        try (MockedStatic<HandlerUtils> utilities = Mockito.mockStatic(HandlerUtils.class)) {

            utilities.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(false);
            utilities.when(() -> HandlerUtils.outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase)).thenReturn(false);

            Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
            Mockito.when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

            Mockito.when(asylumCase.read(
                AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER,
                String.class
            )).thenReturn(Optional.of("1234-5678-9012-3456"));

            Mockito.when(homeOfficeReferenceService.getHomeOfficeReferenceData(
                "1234-5678-9012-3456",
                callback
            )).thenReturn(Optional.of(List.of(new IdValue<>("1", Mockito.mock(HomeOfficeAppellant.class)))));

            PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

            Assertions.assertTrue(response.getErrors().isEmpty());
        }
    }
}