package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY;
import static uk.gov.hmcts.reform.iacaseapi.utils.TestUtils.setupLogVerifier;
import static uk.gov.hmcts.reform.iacaseapi.utils.TestUtils.verifyLogsContainMessage;

@ExtendWith(MockitoExtension.class)
class HomeOfficeReferenceHandlerOnSubmitTest {
    private final String homeOfficeSerialisedEncryptionKey = "test-encryption-key";
    private final String encryptedData = "someData";

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    @Captor
    private ArgumentCaptor<List<IdValue<HomeOfficeAppellant>>> appellantsCaptor;
    private final MockedStatic<HandlerUtils> handlerUtilsMock = Mockito.mockStatic(HandlerUtils.class);

    private HomeOfficeReferenceHandlerOnSubmit handler;

    @BeforeEach
    void setUp() {
        handler = new HomeOfficeReferenceHandlerOnSubmit(homeOfficeSerialisedEncryptionKey);
    }

    @AfterEach
    void tearDown() {
        handlerUtilsMock.close();
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"})
    void canHandle_true_for_correct_stage_and_event(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"}, mode = EnumSource.Mode.EXCLUDE)
    void canHandle_false_for_correct_stage_incorrect_event(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }


    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_SUBMIT"}, mode = EnumSource.Mode.EXCLUDE)
    void canHandle_false_for_incorrect_stage_correct_event(PreSubmitCallbackStage stage) {
        assertFalse(handler.canHandle(stage, callback));
    }

    @Test
    void canHandle_throws_for_null_stage_or_callback() {
        NullPointerException stageException = assertThrows(NullPointerException.class,
                () -> handler.canHandle(null, callback));
        assertEquals("callbackStage must not be null", stageException.getMessage());

        NullPointerException callbackException = assertThrows(NullPointerException.class,
                () -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null));
        assertEquals("callback must not be null", callbackException.getMessage());
    }

    @Test
    void handle_throws_for_cannot_handle() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
        assertEquals("Cannot handle callback", exception.getMessage());
    }

    @Test
    void should_handle_for_empty_appellants_non_empty_serialised() {
        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceHandlerOnSubmit.class);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
                .thenReturn(Optional.of(encryptedData));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        handlerUtilsMock
                .when(() -> HandlerUtils.getUanOrGwf(asylumCase))
                .thenReturn("non-empty-reference");
        String json =
                "[{\"id\":\"123\",\"value\":{\"familyName\":\"Smith\",\"givenNames\":\"John\"}}]";
        handlerUtilsMock
                .when(() -> HandlerUtils.decrypt(encryptedData, homeOfficeSerialisedEncryptionKey))
                .thenReturn(json);

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        verifyLogsContainMessage(listAppender, "Writing previously retrieved Home Office appellant data to the case record in full for case with Home Office reference non-empty-reference.");
        verify(asylumCase).write(eq(HOME_OFFICE_APPELLANTS), appellantsCaptor.capture());

        List<IdValue<HomeOfficeAppellant>> actualList = appellantsCaptor.getValue();
        assertEquals(1, actualList.size());
        IdValue<HomeOfficeAppellant> actualAppellant = actualList.getFirst();
        assertEquals("123", actualAppellant.getId());
        assertEquals("John", actualAppellant.getValue().getGivenNames());
        assertEquals("Smith", actualAppellant.getValue().getFamilyName());
    }

    @Test
    void should_handle_for_empty_appellants_non_empty_serialised_multiple() {
        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceHandlerOnSubmit.class);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
                .thenReturn(Optional.of(encryptedData));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        handlerUtilsMock
                .when(() -> HandlerUtils.getUanOrGwf(asylumCase))
                .thenReturn("non-empty-reference");
        String json =
                "[{\"id\":\"123\",\"value\":{\"familyName\":\"Smith\",\"givenNames\":\"John\"}}," +
                        "{\"id\":\"456\",\"value\":{\"familyName\":\"Doe\",\"givenNames\":\"Jane\"}}]";
        handlerUtilsMock
                .when(() -> HandlerUtils.decrypt(encryptedData, homeOfficeSerialisedEncryptionKey))
                .thenReturn(json);

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        ILoggingEvent loggingEvent = verifyLogsContainMessage(listAppender,
                "Writing previously retrieved Home Office appellant data to the case record in full for case with Home Office reference non-empty-reference.");
        assertEquals(Level.INFO, loggingEvent.getLevel());

        verify(asylumCase).write(eq(HOME_OFFICE_APPELLANTS), appellantsCaptor.capture());

        List<IdValue<HomeOfficeAppellant>> actualList = appellantsCaptor.getValue();
        assertEquals(2, actualList.size());
        IdValue<HomeOfficeAppellant> actualAppellant = actualList.getFirst();
        assertEquals("123", actualAppellant.getId());
        assertEquals("John", actualAppellant.getValue().getGivenNames());
        assertEquals("Smith", actualAppellant.getValue().getFamilyName());
        IdValue<HomeOfficeAppellant> actualAppellantTwo = actualList.getLast();
        assertEquals("456", actualAppellantTwo.getId());
        assertEquals("Jane", actualAppellantTwo.getValue().getGivenNames());
        assertEquals("Doe", actualAppellantTwo.getValue().getFamilyName());
    }

    @Test
    void should_log_error_if_decryption_deserialisation_fails() {
        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceHandlerOnSubmit.class);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
                .thenReturn(Optional.of(encryptedData));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        handlerUtilsMock
                .when(() -> HandlerUtils.getUanOrGwf(asylumCase))
                .thenReturn("non-empty-reference");

        handlerUtilsMock
                .when(() -> HandlerUtils.decrypt(encryptedData, homeOfficeSerialisedEncryptionKey))
                .thenThrow(new RuntimeException("Decryption failed"));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());
        verifyLogsContainMessage(listAppender, "Writing previously retrieved Home Office appellant data to the case record in full for case with Home Office reference non-empty-reference.");
        verify(asylumCase, never()).write(eq(HOME_OFFICE_APPELLANTS), any());
        ILoggingEvent loggingEvent = verifyLogsContainMessage(listAppender,
                "Could not deserialise list of Home Office appellants from encrypted serialised string");
        assertEquals(Level.ERROR, loggingEvent.getLevel());
    }

    @Test
    void should_throw_error_if_home_office_reference_number_empty() {
        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceHandlerOnSubmit.class);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
                .thenReturn(Optional.of(encryptedData));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        handlerUtilsMock
                .when(() -> HandlerUtils.getUanOrGwf(asylumCase))
                .thenReturn("");

        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                () -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));

        assertEquals("homeOfficeReferenceNumber and gwfReferenceNumber are both missing - one or other is needed",
                illegalStateException.getMessage());
        assertTrue(listAppender.list.isEmpty());
    }

    @Test
    void should_do_nothing_if_homeOfficeAppellants_not_empty() {
        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceHandlerOnSubmit.class);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS)).thenReturn(Optional.of(List.of(new IdValue<>("1", new HomeOfficeAppellant()))));
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
                .thenReturn(Optional.of(encryptedData));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());

        verify(asylumCase, never()).write(any(), any());
        assertTrue(listAppender.list.isEmpty());
    }

    @Test
    void should_do_nothing_if_homeOfficeAppellantsSerialisedEncrypted_empty() {
        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceHandlerOnSubmit.class);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
                .thenReturn(Optional.empty());
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());

        verify(asylumCase, never()).write(any(), any());
        assertTrue(listAppender.list.isEmpty());
    }
//
//    @Test
//    void should_handle_about_to_submit_start_appeal() {
//
//        when(callback.getEvent())
//                .thenReturn(Event.START_APPEAL);
//
//        assertEquals(
//                true,
//                handler.canHandle(
//                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                        callback
//                )
//        );
//    }
//
//    @Test
//    void should_handle_about_to_submit_edit_appeal() {
//
//        when(callback.getEvent())
//                .thenReturn(Event.EDIT_APPEAL);
//
//        assertEquals(
//                true,
//                handler.canHandle(
//                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                        callback
//                )
//        );
//    }
//
//    @Test
//    void should_handle_about_to_submit_edit_appeal_after_submit() {
//
//        when(callback.getEvent())
//                .thenReturn(Event.EDIT_APPEAL_AFTER_SUBMIT);
//
//        assertEquals(
//                true,
//                handler.canHandle(
//                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                        callback
//                )
//        );
//    }
//
//    @Test
//    void should_not_handle_wrong_stage() {
//
//        assertFalse(
//                handler.canHandle(
//                        PreSubmitCallbackStage.ABOUT_TO_START,
//                        callback
//                )
//        );
//    }
//
//    @Test
//    void should_not_handle_wrong_event() {
//
//        when(callback.getEvent())
//                .thenReturn(Event.SUBMIT_APPEAL);
//
//        assertFalse(
//                handler.canHandle(
//                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                        callback
//                )
//        );
//    }
//
//    @Test
//    void should_throw_if_callback_stage_null() {
//
//        assertThrows(
//                NullPointerException.class,
//                () -> handler.canHandle(null, callback)
//        );
//    }
//
//    @Test
//    void should_throw_if_callback_null() {
//
//        assertThrows(
//                NullPointerException.class,
//                () -> handler.canHandle(
//                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                        null
//                )
//        );
//    }
//
//    @Test
//    void should_throw_if_cannot_handle() {
//
//        when(callback.getEvent())
//                .thenReturn(Event.SUBMIT_APPEAL);
//
//        assertThrows(
//                IllegalStateException.class,
//                () -> handler.handle(
//                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                        callback
//                )
//        );
//    }
//
//    @Test
//    void should_return_response_without_writing_appellants_when_serialised_data_missing() {
//
//        when(callback.getEvent())
//                .thenReturn(Event.START_APPEAL);
//
//        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
//                .thenReturn(Optional.empty());
//
//        when(asylumCase.read(
//                HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
//                String.class))
//                .thenReturn(Optional.empty());
//
//        when(callback.getCaseDetails())
//                .thenReturn(caseDetails);
//
//        when(caseDetails.getCaseData())
//                .thenReturn(asylumCase);
//
//        PreSubmitCallbackResponse<AsylumCase> response =
//                handler.handle(
//                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                        callback
//                );
//
//        assertEquals(asylumCase, response.getData());
//
//        verify(asylumCase, never())
//                .write(eq(HOME_OFFICE_APPELLANTS), any());
//    }
//
//    @Test
//    void should_deserialise_and_write_appellants_to_case() {
//
//        when(callback.getEvent())
//                .thenReturn(Event.START_APPEAL);
//
//        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
//                .thenReturn(Optional.empty());
//
//        when(asylumCase.read(
//                HOME_OFFICE_REFERENCE_NUMBER,
//                String.class))
//                .thenReturn(Optional.of(HO_REFERENCE));
//
//        String json =
//                "[{\"id\":\"1\",\"value\":{\"familyName\":\"Smith\"}}]";
//        handlerUtilsMock.when(
//                        () -> HandlerUtils.decrypt(encryptedData, homeOfficeSerialisedEncryptionKey))
//                .thenReturn(json);
//
//        when(asylumCase.read(
//                HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
//                String.class))
//                .thenReturn(Optional.of(encryptedData));
//
//        when(callback.getCaseDetails())
//                .thenReturn(caseDetails);
//
//        when(caseDetails.getCaseData())
//                .thenReturn(asylumCase);
//
//        PreSubmitCallbackResponse<AsylumCase> response =
//                handler.handle(
//                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                        callback
//                );
//
//        assertEquals(asylumCase, response.getData());
//
//        verify(asylumCase)
//                .write(
//                        eq(HOME_OFFICE_APPELLANTS),
//                        any(List.class)
//                );
//    }
//
//    @Test
//    void should_not_throw_when_serialised_json_invalid() {
//
//        when(callback.getEvent())
//                .thenReturn(Event.START_APPEAL);
//
//        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
//                .thenReturn(Optional.empty());
//
//        when(asylumCase.read(
//                HOME_OFFICE_REFERENCE_NUMBER,
//                String.class))
//                .thenReturn(Optional.of(HO_REFERENCE));
//
//        when(asylumCase.read(
//                HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
//                String.class))
//                .thenReturn(Optional.of("NOT VALID JSON"));
//
//        when(callback.getCaseDetails())
//                .thenReturn(caseDetails);
//
//        when(caseDetails.getCaseData())
//                .thenReturn(asylumCase);
//
//        assertDoesNotThrow(
//                () -> handler.handle(
//                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                        callback
//                )
//        );
//
//        verify(asylumCase, never())
//                .write(eq(HOME_OFFICE_APPELLANTS), any());
//    }
//
//    @Test
//    void should_correctly_deserialise_real_objects() {
//
//        when(callback.getEvent())
//                .thenReturn(Event.START_APPEAL);
//
//        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
//                .thenReturn(Optional.empty());
//
//        when(asylumCase.read(
//                HOME_OFFICE_REFERENCE_NUMBER,
//                String.class))
//                .thenReturn(Optional.of(HO_REFERENCE));
//
//        when(callback.getCaseDetails())
//                .thenReturn(caseDetails);
//
//        when(caseDetails.getCaseData())
//                .thenReturn(asylumCase);
//
//        String json =
//                "[{\"id\":\"123\",\"value\":{\"familyName\":\"Smith\",\"givenNames\":\"John\"}}]";
//
//        when(asylumCase.read(
//                HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
//                String.class))
//                .thenReturn(Optional.of(HandlerUtils.encrypt(json)));
//
//        handler.handle(
//                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                callback
//        );
//
//        verify(asylumCase)
//                .write(
//                        eq(HOME_OFFICE_APPELLANTS),
//                        any(List.class)
//                );
//    }
//
//    @Test
//    void should_throw_when_handle_called_with_wrong_stage() {
//
//        IllegalStateException exception =
//                assertThrows(
//                        IllegalStateException.class,
//                        () -> handler.handle(
//                                PreSubmitCallbackStage.ABOUT_TO_START,
//                                callback
//                        )
//                );
//
//        assertEquals(
//                "Cannot handle callback",
//                exception.getMessage()
//        );
//    }
//
//    @Test
//    void should_return_empty_response_when_serialised_value_blank() {
//
//        when(callback.getEvent())
//                .thenReturn(Event.START_APPEAL);
//
//        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
//                .thenReturn(Optional.empty());
//
//        when(callback.getCaseDetails())
//                .thenReturn(caseDetails);
//
//        when(caseDetails.getCaseData())
//                .thenReturn(asylumCase);
//
//        when(asylumCase.read(
//                HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
//                String.class))
//                .thenReturn(Optional.of(""));
//
//        PreSubmitCallbackResponse<AsylumCase> response =
//                handler.handle(
//                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                        callback
//                );
//
//        assertEquals(asylumCase, response.getData());
//
//        verify(asylumCase, never())
//                .write(eq(HOME_OFFICE_APPELLANTS), any());
//    }
//
//    @Test
//    void should_write_deserialised_appellants_with_expected_values() {
//
//        when(callback.getEvent())
//                .thenReturn(Event.START_APPEAL);
//
//        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
//                .thenReturn(Optional.empty());
//
//        when(callback.getCaseDetails())
//                .thenReturn(caseDetails);
//
//        when(caseDetails.getCaseData())
//                .thenReturn(asylumCase);
//
//        when(asylumCase.read(
//                HOME_OFFICE_REFERENCE_NUMBER,
//                String.class))
//                .thenReturn(Optional.of(HO_REFERENCE));
//
//        String json =
//                "[{\"id\":\"ABC123\",\"value\":{\"familyName\":\"Smith\",\"givenNames\":\"John\"}}]";
//
//        when(asylumCase.read(
//                HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
//                String.class))
//                .thenReturn(Optional.of(HandlerUtils.encrypt(json)));
//
//        handler.handle(
//                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                callback
//        );
//
//        verify(asylumCase)
//                .write(
//                        eq(HOME_OFFICE_APPELLANTS),
//                        argThat(
//                                (List<IdValue<HomeOfficeAppellant>> appellants) ->
//                                        appellants.size() == 1
//                                                && "ABC123".equals(
//                                                appellants.getFirst().getId())
//                                                && "Smith".equals(
//                                                appellants.getFirst()
//                                                        .getValue()
//                                                        .getFamilyName())
//                                                && "John".equals(
//                                                appellants.getFirst()
//                                                        .getValue()
//                                                        .getGivenNames())
//                        )
//                );
//    }
//
//    @Test
//    void should_handle_malformed_json_exception_branch() {
//
//        when(callback.getEvent())
//                .thenReturn(Event.START_APPEAL);
//
//        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
//                .thenReturn(Optional.empty());
//
//        when(callback.getCaseDetails())
//                .thenReturn(caseDetails);
//
//        when(caseDetails.getCaseData())
//                .thenReturn(asylumCase);
//
//        when(asylumCase.read(
//                HOME_OFFICE_REFERENCE_NUMBER,
//                String.class))
//                .thenReturn(Optional.of(HO_REFERENCE));
//
//        when(asylumCase.read(
//                HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
//                String.class))
//                .thenReturn(Optional.of("{ definitely invalid json"));
//
//        assertDoesNotThrow(
//                () -> handler.handle(
//                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                        callback
//                )
//        );
//
//        verify(asylumCase, never())
//                .write(eq(HOME_OFFICE_APPELLANTS), any());
//    }
//
//    @Test
//    void should_not_deserialise_when_home_office_appellants_already_present() {
//
//        when(callback.getEvent())
//                .thenReturn(Event.START_APPEAL);
//
//        when(callback.getCaseDetails())
//                .thenReturn(caseDetails);
//
//        when(caseDetails.getCaseData())
//                .thenReturn(asylumCase);
//
//        List<IdValue<HomeOfficeAppellant>> existingAppellants =
//                List.of(new IdValue<>("1", new HomeOfficeAppellant()));
//
//        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
//                .thenReturn(Optional.of(existingAppellants));
//
//        when(asylumCase.read(
//                HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
//                String.class))
//                .thenReturn(Optional.of(
//                        "[{\"id\":\"2\",\"value\":{\"familyName\":\"Jones\"}}]"
//                ));
//
//        PreSubmitCallbackResponse<AsylumCase> response =
//                handler.handle(
//                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                        callback
//                );
//
//        assertEquals(asylumCase, response.getData());
//
//        verify(asylumCase, never())
//                .write(eq(HOME_OFFICE_APPELLANTS), any());
//    }
//
//    @Test
//    void should_deserialise_using_gwf_reference_when_home_office_reference_missing() {
//
//        when(callback.getEvent())
//                .thenReturn(Event.START_APPEAL);
//
//        when(callback.getCaseDetails())
//                .thenReturn(caseDetails);
//
//        when(caseDetails.getCaseData())
//                .thenReturn(asylumCase);
//
//        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
//                .thenReturn(Optional.empty());
//
//        when(asylumCase.read(
//                HOME_OFFICE_REFERENCE_NUMBER,
//                String.class))
//                .thenReturn(Optional.empty());
//
//        when(asylumCase.read(
//                GWF_REFERENCE_NUMBER,
//                String.class))
//                .thenReturn(Optional.of("GWF-12345"));
//
//        when(asylumCase.read(
//                HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
//                String.class))
//                .thenReturn(Optional.of(HandlerUtils.encrypt("[{\"id\":\"1\",\"value\":{\"familyName\":\"Smith\"}}]")
//                ));
//
//        handler.handle(
//                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                callback
//        );
//
//        verify(asylumCase)
//                .write(
//                        eq(HOME_OFFICE_APPELLANTS),
//                        any(List.class)
//                );
//    }
//
//    @Test
//    void should_throw_when_home_office_and_gwf_references_both_missing() {
//
//        when(callback.getEvent())
//                .thenReturn(Event.START_APPEAL);
//
//        when(callback.getCaseDetails())
//                .thenReturn(caseDetails);
//
//        when(caseDetails.getCaseData())
//                .thenReturn(asylumCase);
//
//        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
//                .thenReturn(Optional.empty());
//
//        when(asylumCase.read(
//                HOME_OFFICE_REFERENCE_NUMBER,
//                String.class))
//                .thenReturn(Optional.empty());
//
//        when(asylumCase.read(
//                GWF_REFERENCE_NUMBER,
//                String.class))
//                .thenReturn(Optional.empty());
//
//        when(asylumCase.read(
//                HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
//                String.class))
//                .thenReturn(Optional.of(
//                        "[{\"id\":\"1\",\"value\":{\"familyName\":\"Smith\"}}]"
//                ));
//
//        IllegalStateException exception =
//                assertThrows(
//                        IllegalStateException.class,
//                        () -> handler.handle(
//                                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                                callback
//                        )
//                );
//
//        assertEquals(
//                "homeOfficeReferenceNumber and gwfReferenceNumber are both missing - one or other is needed",
//                exception.getMessage()
//        );
//    }
//
//    @Test
//    void should_not_require_reference_numbers_when_appellants_already_exist() {
//
//        when(callback.getEvent())
//                .thenReturn(Event.START_APPEAL);
//
//        when(callback.getCaseDetails())
//                .thenReturn(caseDetails);
//
//        when(caseDetails.getCaseData())
//                .thenReturn(asylumCase);
//
//        List<IdValue<HomeOfficeAppellant>> existingAppellants =
//                List.of(new IdValue<>("1", new HomeOfficeAppellant()));
//
//        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
//                .thenReturn(Optional.of(existingAppellants));
//
//        when(asylumCase.read(
//                HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
//                String.class))
//                .thenReturn(Optional.of(
//                        "[{\"id\":\"2\",\"value\":{\"familyName\":\"Jones\"}}]"
//                ));
//
//        assertDoesNotThrow(
//                () -> handler.handle(
//                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
//                        callback
//                )
//        );
//
//        verify(asylumCase, never())
//                .write(eq(HOME_OFFICE_APPELLANTS), any());
//    }


}