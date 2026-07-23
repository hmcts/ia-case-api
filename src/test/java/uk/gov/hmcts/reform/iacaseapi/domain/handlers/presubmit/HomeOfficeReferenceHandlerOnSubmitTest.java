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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HO_ASYLUM_SUPPORT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HO_FEE_WAIVER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HO_RIGHT_OF_APPEAL;
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

        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceHandlerOnSubmit.class);
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

        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceHandlerOnSubmit.class);
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

        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceHandlerOnSubmit.class);
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
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
                .thenReturn(Optional.of(encryptedData));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        handlerUtilsMock
                .when(() -> HandlerUtils.getUanOrGwf(asylumCase))
                .thenReturn("");

        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceHandlerOnSubmit.class);
        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                () -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));

        assertEquals("homeOfficeReferenceNumber and gwfReferenceNumber are both missing - one or other is needed",
                illegalStateException.getMessage());
        assertTrue(listAppender.list.isEmpty());
    }

    @Test
    void should_do_nothing_if_homeOfficeAppellants_not_empty() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS)).thenReturn(Optional.of(List.of(new IdValue<>("1", new HomeOfficeAppellant()))));
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
                .thenReturn(Optional.of(encryptedData));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceHandlerOnSubmit.class);
        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());

        verify(asylumCase, never()).write(any(), any());
        assertTrue(listAppender.list.isEmpty());
    }

    @Test
    void should_do_nothing_if_homeOfficeAppellantsSerialisedEncrypted_empty() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
                .thenReturn(Optional.empty());
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceHandlerOnSubmit.class);
        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());

        verify(asylumCase, never()).write(any(), any());
        assertTrue(listAppender.list.isEmpty());
    }

    @Test
    void should_write_ho_right_of_appeal_from_serialised_appellant_pp01() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
                .thenReturn(Optional.of(encryptedData));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        handlerUtilsMock.when(() -> HandlerUtils.getUanOrGwf(asylumCase)).thenReturn("non-empty-reference");
        String json =
                "[{\"id\":\"1342-5786-9120-3564/01\",\"value\":{\"pp\":\"01\",\"familyName\":\"Bachchan\",\"givenNames\":\"Abhishek Amitabh\",\"roa\":\"Yes\",\"asylumSupport\":\"No\",\"hoFeeWaiver\":\"Yes\"}}," +
                "{\"id\":\"1342-5786-9120-3564/02\",\"value\":{\"pp\":\"02\",\"familyName\":\"Rai\",\"givenNames\":\"Aishwarya\",\"roa\":\"No\"}}]";
        handlerUtilsMock.when(() -> HandlerUtils.decrypt(encryptedData, homeOfficeSerialisedEncryptionKey)).thenReturn(json);

        handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).write(eq(HO_RIGHT_OF_APPEAL), eq(YesOrNo.YES));
        verify(asylumCase).write(eq(HO_ASYLUM_SUPPORT), eq(YesOrNo.NO));
        verify(asylumCase).write(eq(HO_FEE_WAIVER), eq(YesOrNo.YES));
    }

    @Test
    void should_write_ho_right_of_appeal_from_existing_appellants_pp01() {
        HomeOfficeAppellant appellant = new HomeOfficeAppellant();
        appellant.setPp("01");
        appellant.setRoa(YesOrNo.YES);
        appellant.setAsylumSupport(YesOrNo.NO);
        appellant.setHoFeeWaiver(YesOrNo.YES);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS)).thenReturn(Optional.of(List.of(new IdValue<>("1342-5786-9120-3564/01", appellant))));
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
                .thenReturn(Optional.empty());
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).write(eq(HO_RIGHT_OF_APPEAL), eq(YesOrNo.YES));
        verify(asylumCase).write(eq(HO_ASYLUM_SUPPORT), eq(YesOrNo.NO));
        verify(asylumCase).write(eq(HO_FEE_WAIVER), eq(YesOrNo.YES));
    }

    @Test
    void should_not_write_ho_fields_when_no_appellant_pp01() {
        HomeOfficeAppellant appellant = new HomeOfficeAppellant();
        appellant.setPp("02");
        appellant.setRoa(YesOrNo.NO);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(HOME_OFFICE_APPELLANTS)).thenReturn(Optional.of(List.of(new IdValue<>("1342-5786-9120-3564/02", appellant))));
        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
                .thenReturn(Optional.empty());
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, never()).write(eq(HO_RIGHT_OF_APPEAL), any());
        verify(asylumCase, never()).write(eq(HO_ASYLUM_SUPPORT), any());
        verify(asylumCase, never()).write(eq(HO_FEE_WAIVER), any());
    }
}