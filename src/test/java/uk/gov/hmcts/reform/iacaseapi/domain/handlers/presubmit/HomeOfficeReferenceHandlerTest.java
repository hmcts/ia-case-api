package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.GWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HomeOfficeApiResponseStatusType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeReferenceService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeReferenceHandlerTest {

    private static final String VALID_GWF = "GWF123456789";
    private static final String INVALID_REF = "123";

    @Mock
    private HomeOfficeReferenceService referenceService;

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private IdValue<HomeOfficeAppellant> idValue;

    @Mock
    private HomeOfficeAppellant appellant;

    @InjectMocks
    private HomeOfficeReferenceHandler handler;

    @BeforeEach
    void setup() {
        Mockito.lenient().when(callback.getCaseDetails()).thenReturn(caseDetails);
        Mockito.lenient().when(caseDetails.getCaseData()).thenReturn(asylumCase);

        // Generic stubbing to prevent strict stubbing argument mismatch
        Mockito.lenient()
            .when(asylumCase.read(Mockito.any(), Mockito.<Class<Object>>any()))
            .thenAnswer(invocation -> Optional.empty());        
    }

    @Test
    void canHandle_should_return_true_for_valid_inputs() {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

        boolean result = handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(result);
    }

    @Test
    void canHandle_should_return_false_for_wrong_stage() {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("oocHomeOfficeReferenceNumber");

        boolean result = handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(result);
    }

    @Test
    void canHandle_should_return_false_for_wrong_page() {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("clearlyTheWrongPage");

        boolean result = handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(result);
    }

    @Test
    void canHandle_should_return_false_for_wrong_event() {

        Mockito.when(callback.getEvent()).thenReturn(Event.UPLOAD_SENSITIVE_DOCUMENTS);
        Mockito.when(callback.getPageId()).thenReturn("cuiHomeOfficeReferenceNumber");

        boolean result = handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(result);
    }

    @Test
    void canHandle_should_throw_when_nulls() {

        assertThrows(NullPointerException.class,
            () -> handler.canHandle(null, callback));

        assertThrows(NullPointerException.class,
            () -> handler.canHandle(PreSubmitCallbackStage.MID_EVENT, null));
    }

    @Test
    void handle_should_throw_when_cannot_handle() {

        Mockito.when(callback.getEvent()).thenReturn(Event.LIST_CASE);

        assertThrows(IllegalStateException.class,
            () -> handler.handle(PreSubmitCallbackStage.MID_EVENT, callback));
    }

    @Test
    void handle_should_return_error_when_reference_not_well_formed() {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("cuiHomeOfficeReferenceNumber");

        Mockito.when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(INVALID_REF));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(response.getErrors().isEmpty());
    }

    @Test
    void handle_should_return_error_when_reference_not_real() {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("oocHomeOfficeReferenceNumber");

        Mockito.when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(List.of());

        Mockito.when(asylumCase.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.NOT_FOUND));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(response.getErrors().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "homeOfficeReferenceNumber",
        "oocHomeOfficeReferenceNumber",
        "cuiHomeOfficeReferenceNumber",
        "cuiGwfReferenceNumber"
    })
    void handle_should_remove_validation_fields_and_succeed_when_reference_is_real(String pageId) {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn(pageId);

        Mockito.when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        try (MockedStatic<HandlerUtils> mockedStatic = Mockito.mockStatic(HandlerUtils.class)) {

            PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

            mockedStatic.verify(() -> HandlerUtils.removeValidationFields(asylumCase));

            assertTrue(response.getErrors().isEmpty());
        }
    }

    @Test
    void handle_should_fail_when_appellant_details_do_not_match() {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("appellantBasicDetails");

        Mockito.when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        Mockito.when(idValue.getValue()).thenReturn(appellant);

        Mockito.when(appellant.getFamilyName()).thenReturn(null);
        Mockito.when(appellant.getGivenNames()).thenReturn(null);
        Mockito.when(appellant.getDateOfBirth()).thenReturn(null);

        Mockito.when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Smith"));

        Mockito.when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("John"));

        Mockito.when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("1990-01-01"));
            
        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(response.getErrors().isEmpty());
    }

    @Test
    void handle_should_fail_when_appellant_name_does_not_match() {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("cuiAppellantName");

        Mockito.when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        Mockito.when(asylumCase.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.OK));

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        Mockito.when(idValue.getValue()).thenReturn(appellant);

        Mockito.when(appellant.getFamilyName()).thenReturn(null);
        Mockito.when(appellant.getGivenNames()).thenReturn(null);
        Mockito.when(appellant.getDateOfBirth()).thenReturn("1990-01-01");

        Mockito.when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Smith"));

        Mockito.when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("John"));

        Mockito.when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("1990-01-01"));
            
        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(response.getErrors().isEmpty());
        assertEquals(
            "The information entered does not match the details held by the Home Office for reference number GWF123456789.  " +
                "You should enter the details exactly as they appear on the decision letter, so that we can verify them.  " +
                "These details can often be found in the 'How to appeal' section.  If you need help, please use the Home Office help form in the bullet points on this page.",
            response.getErrors().iterator().next());
    }

    @Test
    void handle_should_fail_when_appellant_dob_does_not_match() {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("cuiAppellantDob");

        Mockito.when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        Mockito.when(idValue.getValue()).thenReturn(appellant);

        Mockito.when(appellant.getFamilyName()).thenReturn("Smith");
        Mockito.when(appellant.getGivenNames()).thenReturn("John");
        Mockito.when(appellant.getDateOfBirth()).thenReturn("1980-01-01");

        Mockito.when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Smith"));

        Mockito.when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("John"));

        Mockito.when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("1990-01-01"));
            
        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(response.getErrors().isEmpty());
        assertEquals(
            "An error occurred.  Please report this to HMCTS using the following contact details: Email contactia@justice.gov.uk or Telephone: 0300 123 1711.",
            response.getErrors().iterator().next());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "appellantBasicDetails", "cuiAppellantName", "cuiAppellantDob"
    })
    void handle_should_succeed_when_appellant_details_match(String pageId) {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn(pageId);

        Mockito.when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        Mockito.when(idValue.getValue()).thenReturn(appellant);

        Mockito.when(appellant.getFamilyName()).thenReturn("Smith");
        Mockito.when(appellant.getGivenNames()).thenReturn("John");
        Mockito.when(appellant.getDateOfBirth()).thenReturn("1990-01-01");

        Mockito.when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Smith"));

        Mockito.when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("John"));

        Mockito.when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("1990-01-01"));
            
        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(response.getErrors().isEmpty());
    }
   
    @Test
    void isWellFormedHomeOfficeReference_should_validate_patterns() {

        assertTrue(HomeOfficeReferenceHandler.isWellFormedHomeOfficeReference("1234-1234-1234-1234"));
        assertTrue(HomeOfficeReferenceHandler.isWellFormedHomeOfficeReference("GWF123456789"));

        assertFalse(HomeOfficeReferenceHandler.isWellFormedHomeOfficeReference("BADREF"));
        assertFalse(HomeOfficeReferenceHandler.isWellFormedHomeOfficeReference(null));
    }

    @Test
    void isRealHomeOfficeCaseNumber_should_return_false_when_null() {

        assertFalse(handler.isRealHomeOfficeCaseNumber(null, callback));
    }

    @Test
    void isRealHomeOfficeCaseNumber_should_return_false_when_empty_response() {

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(List.of());

        assertFalse(handler.isRealHomeOfficeCaseNumber(VALID_GWF, callback));
    }

    @Test
    void isRealHomeOfficeCaseNumber_should_return_true_when_appellants_exist() {

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        assertTrue(handler.isRealHomeOfficeCaseNumber(VALID_GWF, callback));
    }

    @Test
    void normaliseName_should_return_empty_string_on_null() {

        String result = HomeOfficeReferenceHandler.normaliseName(null, false);

        assertEquals("", result);
    }

    @Test
    void normaliseName_should_remove_accents_and_spaces() {

        String result = HomeOfficeReferenceHandler.normaliseName(" José   García ", false);

        assertEquals("jose garcia", result);
    }

    @Test
    void isMatchingNameAndDob_should_match() {

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        Mockito.when(idValue.getValue()).thenReturn(appellant);

        Mockito.when(appellant.getFamilyName()).thenReturn("Smith");
        Mockito.when(appellant.getGivenNames()).thenReturn("John");
        Mockito.when(appellant.getDateOfBirth()).thenReturn("1990-01-01");

        Mockito.when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Smith"));

        Mockito.when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("John"));

        Mockito.when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("1990-01-01"));

        boolean result = handler.isMatchingNameAndDob(VALID_GWF, asylumCase, callback);

        assertTrue(result);
    }

    @Test
    void isMatchingNameAndDob_should_not_match() {

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        Mockito.when(idValue.getValue()).thenReturn(appellant);

        Mockito.when(appellant.getFamilyName()).thenReturn("Different");
        Mockito.when(appellant.getGivenNames()).thenReturn("Person");
        Mockito.when(appellant.getDateOfBirth()).thenReturn("1980-01-01");

        Mockito.when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Smith"));

        Mockito.when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("John"));

        Mockito.when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("1990-01-01"));

        boolean result = handler.isMatchingNameAndDob(VALID_GWF, asylumCase, callback);

        assertFalse(result);
    }

    @Test
    void isMatchingName_should_match_when_given_names_have_different_second_words() {

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        Mockito.when(idValue.getValue()).thenReturn(appellant);

        Mockito.when(appellant.getFamilyName()).thenReturn("Smith");
        Mockito.when(appellant.getGivenNames()).thenReturn("John Boy");

        Mockito.when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Smith"));

        Mockito.when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("John James"));

        boolean result = handler.isMatchingName(VALID_GWF, asylumCase, callback);

        assertTrue(result);
    }

    @Test
    void isMatchingName_should_return_false_when_family_name_has_different_second_word() {

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        Mockito.when(idValue.getValue()).thenReturn(appellant);

        Mockito.when(appellant.getFamilyName()).thenReturn("Smithsonian Institute");
        Mockito.when(appellant.getGivenNames()).thenReturn("John");

        Mockito.when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Smithsonian"));

        Mockito.when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("John"));

        boolean result = handler.isMatchingName(VALID_GWF, asylumCase, callback);

        assertFalse(result);
    }

    @Test
    void isMatchingNameAndDob_should_return_false_when_family_name_is_null() {

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        Mockito.when(idValue.getValue()).thenReturn(appellant);

        Mockito.when(appellant.getFamilyName()).thenReturn(null);
        Mockito.when(appellant.getGivenNames()).thenReturn(null);
        Mockito.when(appellant.getDateOfBirth()).thenReturn(null);

        Mockito.when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Smithsonian"));

        Mockito.when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("John"));

        Mockito.when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("1990-01-01"));

        boolean result = handler.isMatchingNameAndDob(VALID_GWF, asylumCase, callback);

        assertFalse(result);
    }

    @Test
    void handle_should_use_gwf_reference_when_home_office_reference_missing() {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

        Mockito.when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.empty());

        Mockito.when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void handle_should_throw_when_home_office_and_gwf_references_missing() {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

        Mockito.when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.empty());

        Mockito.when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> handler.handle(PreSubmitCallbackStage.MID_EVENT, callback)
        );

        assertEquals(
            "homeOfficeReferenceNumber and gwfReferenceNumber are both missing - one or other is needed",
            ex.getMessage()
        );
    }

    @Test
    void handle_should_return_mismatch_error_when_details_do_not_match_and_status_is_ok() {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("cuiAppellantName");

        Mockito.when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        Mockito.when(idValue.getValue()).thenReturn(appellant);

        Mockito.when(appellant.getFamilyName()).thenReturn("Smith");
        Mockito.when(appellant.getGivenNames()).thenReturn("John");

        Mockito.when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Jones"));

        Mockito.when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("Fred"));

        Mockito.when(asylumCase.read(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.OK));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertEquals(1, response.getErrors().size());

        assertTrue(
            response.getErrors()
            .stream()
            .anyMatch(error -> error.contains("does not match the details held by the Home Office"))
        );
    }

    @Test
    void handle_should_return_api_error_when_details_do_not_match_and_status_not_ok() {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("cuiAppellantName");

        Mockito.when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        Mockito.when(idValue.getValue()).thenReturn(appellant);

        Mockito.when(appellant.getFamilyName()).thenReturn("Smith");
        Mockito.when(appellant.getGivenNames()).thenReturn("John");

        Mockito.when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Jones"));

        Mockito.when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("Fred"));

        Mockito.when(asylumCase.read(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.NOT_FOUND));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertEquals(1, response.getErrors().size());

        assertTrue(
            response.getErrors()
            .stream()
            .anyMatch(error -> error.contains(HomeOfficeApiResponseStatusType.NOT_FOUND.getUserFacingErrorText(VALID_GWF)))
        );
    }

    @Test
    void handle_should_use_unknown_status_when_response_status_missing() {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("cuiAppellantName");

        Mockito.when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        Mockito.when(idValue.getValue()).thenReturn(appellant);

        Mockito.when(appellant.getFamilyName()).thenReturn("Smith");
        Mockito.when(appellant.getGivenNames()).thenReturn("John");

        Mockito.when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Jones"));

        Mockito.when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("Fred"));

        Mockito.when(asylumCase.read(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertEquals(1, response.getErrors().size());

        assertTrue(
            response.getErrors()
            .stream()
            .anyMatch(error -> error.contains(HomeOfficeApiResponseStatusType.UNKNOWN.getUserFacingErrorText(VALID_GWF)))
        );
    }

    @Test
    void handle_should_remove_validation_fields_before_validating_home_office_reference() {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

        Mockito.when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        try (MockedStatic<HandlerUtils> mockedStatic = Mockito.mockStatic(HandlerUtils.class)) {

            PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

            mockedStatic.verify(() -> HandlerUtils.removeValidationFields(asylumCase));

            assertTrue(response.getErrors().isEmpty());
        }
    }
}