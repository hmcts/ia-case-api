package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.getMismatchErrorMessage;

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
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void canHandle_should_return_true_for_valid_inputs() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

        boolean result = handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(result);
    }

    @Test
    void canHandle_should_return_false_for_wrong_stage() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("oocHomeOfficeReferenceNumber");

        boolean result = handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(result);
    }

    @Test
    void canHandle_should_return_false_for_wrong_page() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("clearlyTheWrongPage");

        boolean result = handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(result);
    }

    @Test
    void canHandle_should_return_false_for_wrong_event() {

        when(callback.getEvent()).thenReturn(Event.UPLOAD_SENSITIVE_DOCUMENTS);
        when(callback.getPageId()).thenReturn("cuiHomeOfficeReferenceNumber");

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

        when(callback.getEvent()).thenReturn(Event.LIST_CASE);

        assertThrows(IllegalStateException.class,
            () -> handler.handle(PreSubmitCallbackStage.MID_EVENT, callback));
    }

    @Test
    void handle_should_return_error_when_reference_not_well_formed() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("cuiHomeOfficeReferenceNumber");

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(INVALID_REF));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(response.getErrors().isEmpty());
    }

    @Test
    void handle_should_return_error_when_reference_not_real() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("oocHomeOfficeReferenceNumber");

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(List.of());

        when(asylumCase.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class))
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

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn(pageId);

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        try (MockedStatic<HandlerUtils> mockedStatic = Mockito.mockStatic(HandlerUtils.class, Mockito.CALLS_REAL_METHODS)) {

            PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

            mockedStatic.verify(() -> HandlerUtils.removeValidationFields(asylumCase));

            assertTrue(response.getErrors().isEmpty());
        }
    }

    @Test
    void handle_should_fail_when_appellant_details_do_not_match() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("appellantBasicDetails");

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        when(idValue.getValue()).thenReturn(appellant);

        when(appellant.getFamilyName()).thenReturn(null);
        when(appellant.getGivenNames()).thenReturn(null);
        when(appellant.getDateOfBirth()).thenReturn(null);

        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Smith"));

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("John"));

        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("1990-01-01"));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(response.getErrors().isEmpty());
    }

    @Test
    void handle_should_fail_when_appellant_name_does_not_match() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("cuiAppellantName");

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        when(asylumCase.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.OK));

        when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        when(idValue.getValue()).thenReturn(appellant);

        when(appellant.getFamilyName()).thenReturn(null);
        when(appellant.getGivenNames()).thenReturn(null);
        when(appellant.getDateOfBirth()).thenReturn("1990-01-01");

        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Smith"));

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("John"));

        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("1990-01-01"));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertFalse(response.getErrors().isEmpty());
        assertEquals(getMismatchErrorMessage(VALID_GWF, true , false), response.getErrors().iterator().next());
    }

    @Test
    void handle_should_fail_when_appellant_dob_does_not_match() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("cuiAppellantDob");

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        when(idValue.getValue()).thenReturn(appellant);

        when(appellant.getFamilyName()).thenReturn("Smith");
        when(appellant.getGivenNames()).thenReturn("John");
        when(appellant.getDateOfBirth()).thenReturn("1980-01-01");

        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Smith"));

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("John"));

        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class))
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

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn(pageId);

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        when(idValue.getValue()).thenReturn(appellant);

        when(appellant.getFamilyName()).thenReturn("Smith");
        when(appellant.getGivenNames()).thenReturn("John");
        when(appellant.getDateOfBirth()).thenReturn("1990-01-01");

        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Smith"));

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("John"));

        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("1990-01-01"));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void handle_should_use_gwf_reference_when_home_office_reference_missing() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.empty());

        when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void handle_should_throw_when_home_office_and_gwf_references_missing() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.empty());

        when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class))
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

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("cuiAppellantName");

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        when(idValue.getValue()).thenReturn(appellant);

        when(appellant.getFamilyName()).thenReturn("Smith");
        when(appellant.getGivenNames()).thenReturn("John");

        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Jones"));

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("Fred"));

        when(asylumCase.read(
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

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("cuiAppellantName");

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        when(idValue.getValue()).thenReturn(appellant);

        when(appellant.getFamilyName()).thenReturn("Smith");
        when(appellant.getGivenNames()).thenReturn("John");

        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Jones"));

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("Fred"));

        when(asylumCase.read(
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

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("cuiAppellantName");

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        when(idValue.getValue()).thenReturn(appellant);

        when(appellant.getFamilyName()).thenReturn("Smith");
        when(appellant.getGivenNames()).thenReturn("John");

        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Jones"));

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("Fred"));

        when(asylumCase.read(
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

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenReturn(Collections.singletonList(idValue));

        try (MockedStatic<HandlerUtils> mockedStatic = Mockito.mockStatic(HandlerUtils.class, Mockito.CALLS_REAL_METHODS)) {

            PreSubmitCallbackResponse<AsylumCase> response =
                handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

            mockedStatic.verify(() -> HandlerUtils.removeValidationFields(asylumCase));

            assertTrue(response.getErrors().isEmpty());
        }
    }

    @Test
    void handle_should_return_unknown_error_when_home_office_validation_throws_exception() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("homeOfficeReferenceNumber");

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenThrow(new RuntimeException("Boom"));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertEquals(1, response.getErrors().size());
        assertTrue(
            response.getErrors().contains(
                HomeOfficeApiResponseStatusType.UNKNOWN.getUserFacingErrorText(VALID_GWF)
            )
        );
    }

    @Test
    void handle_should_return_unknown_error_when_name_validation_throws_exception() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("cuiAppellantName");

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenThrow(new RuntimeException("Boom"));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertEquals(
            HomeOfficeApiResponseStatusType.UNKNOWN.getUserFacingErrorText(VALID_GWF),
            response.getErrors().iterator().next()
        );
    }

    @Test
    void handle_should_return_unknown_error_when_name_and_dob_validation_throws_exception() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getPageId()).thenReturn("cuiAppellantDob");

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(VALID_GWF));

        when(referenceService.getHomeOfficeReferenceData(VALID_GWF, callback))
            .thenThrow(new RuntimeException("Boom"));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertEquals(1, response.getErrors().size());

        assertTrue(
            response.getErrors()
                .contains(HomeOfficeApiResponseStatusType.UNKNOWN.getUserFacingErrorText(VALID_GWF))
        );
    }

}