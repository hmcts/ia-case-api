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

    private HomeOfficeReferenceService service;
    private HomeOfficeReferenceHandler handler;
    private Callback<AsylumCase> callback;
    private CaseDetails<AsylumCase> caseDetails;
    private AsylumCase asylumCase;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() {
        service = Mockito.mock(HomeOfficeReferenceService.class);
        handler = new HomeOfficeReferenceHandler(service);

        callback = Mockito.mock(Callback.class);
        caseDetails = Mockito.mock(CaseDetails.class);
        asylumCase = Mockito.mock(AsylumCase.class);

        Mockito.when(callback.getCaseDetails()).thenReturn(caseDetails);
        Mockito.when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    // ---------------------------------------------------------
    // canHandle coverage (all branches)
    // ---------------------------------------------------------

    @Test
    void canHandleTrueForEditAppealAndOocPage() {
        Mockito.when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("oocHomeOfficeReferenceNumber");

        Assertions.assertTrue(
            handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback)
        );
    }

    @Test
    void canHandleFalseForWrongPage() {
        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("wrongPage");

        Assertions.assertFalse(
            handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback)
        );
    }

    // ---------------------------------------------------------
    // handle — skip validation branch
    // ---------------------------------------------------------

    @Test
    void handleSkipsValidationWhenInternalAndEntryClearance() {

        try (MockedStatic<HandlerUtils> utils = Mockito.mockStatic(HandlerUtils.class)) {

            utils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(true);
            utils.when(() -> HandlerUtils.isEntryClearanceDecision(asylumCase)).thenReturn(true);

            Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
            Mockito.when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

            PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

            Assertions.assertTrue(response.getErrors().isEmpty());
        }
    }

    // ---------------------------------------------------------
    // reference missing
    // ---------------------------------------------------------

    @Test
    void handleThrowsIfReferenceMissing() {

        try (MockedStatic<HandlerUtils> utils = Mockito.mockStatic(HandlerUtils.class)) {

            utils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(false);
            utils.when(() -> HandlerUtils.outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase)).thenReturn(false);

            Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
            Mockito.when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

            Mockito.when(asylumCase.read(
                AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER,
                String.class
            )).thenReturn(Optional.empty());

            Assertions.assertThrows(
                IllegalStateException.class,
                () -> handler.handle(PreSubmitCallbackStage.MID_EVENT, callback)
            );
        }
    }

    // ---------------------------------------------------------
    // case number error message switch coverage
    // ---------------------------------------------------------

    @Test
    void handleReturnsCaseNotFoundMessage() {

        try (MockedStatic<HandlerUtils> utils = Mockito.mockStatic(HandlerUtils.class)) {

            utils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(false);
            utils.when(() -> HandlerUtils.outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase)).thenReturn(false);

            Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
            Mockito.when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

            String ref = "1234-5678-9012-3456";

            Mockito.when(asylumCase.read(
                AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER,
                String.class
            )).thenReturn(Optional.of(ref));

            Mockito.when(service.getHomeOfficeReferenceData(ref, callback))
                .thenThrow(new HomeOfficeMissingApplicationException(404, "msg"));

            PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

            Assertions.assertTrue(
                response.getErrors().stream()
                    .anyMatch(error -> error.contains("does not match any existing case records"))
            );
        }
    }

    // ---------------------------------------------------------
    // appellantBasicDetails branch
    // ---------------------------------------------------------

    @Test
    void handleReturnsDetailsMismatchError() {

        try (MockedStatic<HandlerUtils> utils = Mockito.mockStatic(HandlerUtils.class)) {

            utils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(false);
            utils.when(() -> HandlerUtils.outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase)).thenReturn(false);

            Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
            Mockito.when(callback.getPageId()).thenReturn("appellantBasicDetails");

            String ref = "1234-5678-9012-3456";

            Mockito.when(asylumCase.read(
                AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER,
                String.class
            )).thenReturn(Optional.of(ref));

            HomeOfficeAppellant appellant = Mockito.mock(HomeOfficeAppellant.class);
            Mockito.when(appellant.getFamilyName()).thenReturn("Smith");
            Mockito.when(appellant.getGivenNames()).thenReturn("John");
            Mockito.when(appellant.getDateOfBirth()).thenReturn("1990-01-01");

            Mockito.when(service.getHomeOfficeReferenceData(ref, callback))
                .thenReturn(Optional.of(List.of(new IdValue<>("1", appellant))));

            Mockito.when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("Wrong"));

            Mockito.when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of("Person"));

            Mockito.when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH, String.class))
                .thenReturn(Optional.of("2000-01-01"));

            PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

            Assertions.assertTrue(
                response.getErrors().stream()
                    .anyMatch(error -> error.contains("details provided do not match"))
            );
        }
    }

    // ---------------------------------------------------------
    // empty optional + empty list branches
    // ---------------------------------------------------------

    @Test
    void caseDetailsReturnsFalseWhenEmptyOptional() {
        Mockito.when(service.getHomeOfficeReferenceData("REF", callback))
            .thenReturn(Optional.empty());

        Assertions.assertFalse(
            handler.isMatchingHomeOfficeCaseDetails("REF", asylumCase, callback)
        );
    }

    @Test
    void caseDetailsReturnsFalseWhenEmptyList() {
        Mockito.when(service.getHomeOfficeReferenceData("REF", callback))
            .thenReturn(Optional.of(Collections.emptyList()));

        Assertions.assertFalse(
            handler.isMatchingHomeOfficeCaseDetails("REF", asylumCase, callback)
        );
    }

}