package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
            .thenReturn(Optional.empty());

        Mockito.when(asylumCase.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.NOT_FOUND));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(response.getErrors().isEmpty());
    }

    @Test
    void handle_should_succeed_when_reference_is_real() {

        Mockito.when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        Mockito.when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

        Mockito.when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Optional.of(Collections.singletonList(idValue)));

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
            .thenReturn(Optional.empty());

        assertFalse(handler.isRealHomeOfficeCaseNumber(VALID_GWF, callback));
    }

    @Test
    void isRealHomeOfficeCaseNumber_should_return_true_when_appellants_exist() {

        Mockito.when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Optional.of(Collections.singletonList(idValue)));

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
            .thenReturn(Optional.of(Collections.singletonList(idValue)));

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
            .thenReturn(Optional.of(Collections.singletonList(idValue)));

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
            .thenReturn(Optional.of(Collections.singletonList(idValue)));

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
            .thenReturn(Optional.of(Collections.singletonList(idValue)));

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
            .thenReturn(Optional.of(Collections.singletonList(idValue)));

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
}