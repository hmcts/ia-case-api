package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeReferenceService;

class HomeOfficeReferenceHandlerTest {

    private HomeOfficeReferenceService service;
    private HomeOfficeReferenceHandler handler;

    private Callback<AsylumCase> callback;
    private CaseDetails<AsylumCase> caseDetails;
    private AsylumCase asylumCase;

    private static final String VALID_REF = "1234-1234-1234-1234";

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() {
        service = Mockito.mock(HomeOfficeReferenceService.class);
        handler = new HomeOfficeReferenceHandler(service);

        callback = (Callback<AsylumCase>) Mockito.mock(Callback.class);
        caseDetails = (CaseDetails<AsylumCase>) Mockito.mock(CaseDetails.class);
        asylumCase = Mockito.mock(AsylumCase.class);

        Mockito.when(callback.getCaseDetails()).thenReturn(caseDetails);
        Mockito.when(caseDetails.getCaseData()).thenReturn(asylumCase);

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");
    }

    // ---------------- canHandle ----------------

    @Test
    void canHandle_allValidCombinations() {
        Assertions.assertTrue(handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback));

        Mockito.when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        Assertions.assertTrue(handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback));

        Mockito.when(callback.getPageId()).thenReturn("oocHomeOfficeReferenceNumber");
        Assertions.assertTrue(handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback));

        Mockito.when(callback.getPageId()).thenReturn("appellantBasicDetails");
        Assertions.assertTrue(handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback));
    }

    @Test
    void canHandle_negativePaths() {
        Assertions.assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));

        Mockito.when(callback.getPageId()).thenReturn("badPage");
        Assertions.assertFalse(handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback));
    }

    // ---------------- guard clause ----------------

    @Test
    void handle_throwsIfCannotHandle() {
        Assertions.assertThrows(IllegalStateException.class,
                () -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    // ---------------- reference validation ----------------

    @Test
    void invalidReferenceFormat_returnsError() {

        stubReference("BAD");

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        Assertions.assertFalse(response.getErrors().isEmpty());
    }

    @Test
    void validReferencePattern() {
        Assertions.assertTrue(HomeOfficeReferenceHandler.isWellFormedHomeOfficeReference(VALID_REF));
        Assertions.assertTrue(HomeOfficeReferenceHandler.isWellFormedHomeOfficeReference("GWF123456789"));
        Assertions.assertFalse(HomeOfficeReferenceHandler.isWellFormedHomeOfficeReference("X"));
        Assertions.assertFalse(HomeOfficeReferenceHandler.isWellFormedHomeOfficeReference(null));
    }

    // ---------------- case number matching ----------------

    @Test
    void matchingCaseNumber_success() {

        stubReference(VALID_REF);

        Mockito.when(service.getHomeOfficeReferenceData(Mockito.anyString(), Mockito.eq(callback)))
                .thenReturn(Optional.of(List.of(new IdValue<>("1", mockAppellant("01", "Silver", "Long John", "1965-02-03", "BRA", YesOrNo.YES, YesOrNo.NO, YesOrNo.NO, "spa", YesOrNo.YES)))));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        Assertions.assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void matchingCaseNumber_emptyOptional() {

        stubReference(VALID_REF);

        Mockito.when(service.getHomeOfficeReferenceData(Mockito.anyString(), Mockito.eq(callback)))
                .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        Assertions.assertFalse(response.getErrors().isEmpty());
    }

    @Test
    void matchingCaseNumber_emptyList() {

        stubReference(VALID_REF);

        Mockito.when(service.getHomeOfficeReferenceData(Mockito.anyString(), Mockito.eq(callback)))
                .thenReturn(Optional.of(Collections.emptyList()));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        Assertions.assertFalse(response.getErrors().isEmpty());
    }

    // ---------------- HTTP status buckets ----------------

    @Test
    void httpStatus_caseNotFound_404() {
        assertExceptionBucket(404, "does not match any existing case records");
    }

    @Test
    void httpStatus_clientError_400() {
        assertExceptionBucket(400, "report this to HMCTS");
    }

    @Test
    void httpStatus_clientError_default999() {
        assertExceptionBucket(999, "report this to HMCTS");
    }

    @Test
    void httpStatus_serverError_500() {
        assertExceptionBucket(500, "try again in 15-20 minutes");
    }

    @Test
    void httpStatus_serverError_timeoutMinus1() {
        assertExceptionBucket(-1, "try again in 15-20 minutes");
    }

    // ---------------- details matching page ----------------

    @Test
    void detailsPage_successMatch() {

        switchToDetailsPage();
        stubReference(VALID_REF);
        stubCaseDetails("Silver", "Long John", "1965-02-03");

        HomeOfficeAppellant ho = mockAppellant("01", "Silver", "Long John", "1965-02-03", "BRA", YesOrNo.YES, YesOrNo.NO, YesOrNo.NO, "spa", YesOrNo.YES);

        Mockito.when(service.getHomeOfficeReferenceData(Mockito.anyString(), Mockito.eq(callback)))
                .thenReturn(Optional.of(List.of(new IdValue<>("1", ho))));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        Assertions.assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void detailsPage_nameMismatch_triggersDefaultBranch() {

        switchToDetailsPage();
        stubReference(VALID_REF);
        stubCaseDetails("Different", "Person", "1990-01-01");

        HomeOfficeAppellant ho = mockAppellant("01", "Silver", "Long John", "1965-02-03", "BRA", YesOrNo.YES, YesOrNo.NO, YesOrNo.NO, "spa", YesOrNo.YES);

        Mockito.when(service.getHomeOfficeReferenceData(Mockito.anyString(), Mockito.eq(callback)))
                .thenReturn(Optional.of(List.of(new IdValue<>("1", ho))));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        Assertions.assertTrue(
                response.getErrors().stream()
                        .anyMatch(e -> e.contains("details provided do not match")));
    }

    @Test
    void detailsPage_exceptionBucket() {

        switchToDetailsPage();
        stubReference(VALID_REF);

        Mockito.when(service.getHomeOfficeReferenceData(Mockito.anyString(), Mockito.eq(callback)))
                .thenThrow(new HomeOfficeMissingApplicationException(404, "err"));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        Assertions.assertFalse(response.getErrors().isEmpty());
    }

    // ---------------- direct method branch coverage ----------------

    @Test
    void isMatchingCaseNumber_nullReference() {
        Assertions.assertFalse(handler.isMatchingHomeOfficeCaseNumber(null, callback));
    }

    @Test
    void isMatchingCaseDetails_nullReference() {
        Assertions.assertFalse(handler.isMatchingHomeOfficeCaseDetails(null, asylumCase, callback));
    }

    @Test
    void normalizeName_branches() {
        Assertions.assertEquals("", HomeOfficeReferenceHandler.normalizeName(null));
        Assertions.assertEquals("jose garcia",
                HomeOfficeReferenceHandler.normalizeName("  José   García "));
    }

    // ---------------- helpers ----------------

    private void stubReference(String ref) {
        Mockito.when(asylumCase.read(
                AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER,
                String.class))
                .thenReturn(Optional.ofNullable(ref));
    }

    private void stubCaseDetails(String family, String given, String dob) {

        Mockito.when(asylumCase.read(
                AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME,
                String.class)).thenReturn(Optional.ofNullable(family));

        Mockito.when(asylumCase.read(
                AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES,
                String.class)).thenReturn(Optional.ofNullable(given));

        Mockito.when(asylumCase.read(
                AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH,
                String.class)).thenReturn(Optional.ofNullable(dob));
    }

    private void switchToDetailsPage() {
        Mockito.when(callback.getPageId()).thenReturn("appellantBasicDetails");
    }

    private void assertExceptionBucket(int status, String expectedText) {

        stubReference(VALID_REF);

        Mockito.when(service.getHomeOfficeReferenceData(Mockito.anyString(), Mockito.eq(callback)))
                .thenThrow(new HomeOfficeMissingApplicationException(status, "error"));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        Assertions.assertTrue(
                response.getErrors().stream()
                        .anyMatch(e -> e.contains(expectedText)));
    }

    private HomeOfficeAppellant mockAppellant(String pp, String familyName, String givenNames, String dateOfBirth, String nationality, YesOrNo roa, YesOrNo asylumSupport, YesOrNo hoFeeWaiver, String language, YesOrNo interpreterNeeded) {
        HomeOfficeAppellant appellant = new HomeOfficeAppellant();
        appellant.setPp(pp);
        appellant.setFamilyName(familyName);
        appellant.setGivenNames(givenNames);
        appellant.setDateOfBirth(dateOfBirth);
        appellant.setNationality(nationality);
        appellant.setRoa(roa);
        appellant.setAsylumSupport(asylumSupport);
        appellant.setHoFeeWaiver(hoFeeWaiver);
        appellant.setLanguage(language);
        appellant.setInterpreterNeeded(interpreterNeeded);
        return appellant;
    }

}