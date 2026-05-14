package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_ID_LIST;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CcdDataService;

class RetriggerWaTasksForFixedCaseIdHandlerTest {

    private CcdDataService ccdDataService;
    private RetriggerWaTasksForFixedCaseIdHandler handler;

    private Callback<AsylumCase> callback;
    private CaseDetails<AsylumCase> caseDetails;
    private AsylumCase asylumCase;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        ccdDataService = mock(CcdDataService.class);
        handler = new RetriggerWaTasksForFixedCaseIdHandler(ccdDataService);

        callback = (Callback<AsylumCase>) mock(Callback.class);
        caseDetails = (CaseDetails<AsylumCase>) mock(CaseDetails.class);
        asylumCase = mock(AsylumCase.class);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    // =====================================================
    // Constructor
    // =====================================================

    @Test
    void should_construct_successfully() {
        CcdDataService service = mock(CcdDataService.class);
        RetriggerWaTasksForFixedCaseIdHandler instance =
            new RetriggerWaTasksForFixedCaseIdHandler(service);

        assertNotNull(instance);
    }

    // =====================================================
    // canHandle()
    // =====================================================

    @Test
    void canHandle_should_return_true_for_about_to_submit_and_correct_event() {

        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);

        boolean result = handler.canHandle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        assertTrue(result);
    }

    @Test
    void canHandle_should_return_false_for_wrong_stage() {

        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);

        boolean result = handler.canHandle(
            PreSubmitCallbackStage.ABOUT_TO_START,
            callback
        );

        assertFalse(result);
    }

    @Test
    void canHandle_should_return_false_for_wrong_event() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        boolean result = handler.canHandle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        assertFalse(result);
    }

    @Test
    void canHandle_should_throw_when_stage_is_null() {

        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);

        NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> handler.canHandle(null, callback)
        );

        assertEquals("callbackStage must not be null", ex.getMessage());
    }

    @Test
    void canHandle_should_throw_when_callback_is_null() {

        NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> handler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                null
            )
        );

        assertEquals("callback must not be null", ex.getMessage());
    }

    // =====================================================
    // handle()
    // =====================================================

    @Test
    void handle_should_throw_when_cannot_handle() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertThrows(
            IllegalStateException.class,
            () -> handler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            )
        );

        verifyNoInteractions(ccdDataService);
    }

    @Test
    void handle_should_return_without_event_when_optional_empty() {

        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);
        when(asylumCase.read(CASE_ID_LIST, String.class))
            .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);

        verifyNoInteractions(ccdDataService);
        verify(asylumCase, never()).clear(CASE_ID_LIST);
    }

    @Test
    void handle_should_return_without_event_when_single_invalid_id() {

        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);
        when(asylumCase.read(CASE_ID_LIST, String.class))
            .thenReturn(Optional.of("123")); // invalid length

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);

        verifyNoInteractions(ccdDataService);
        verify(asylumCase, never()).clear(CASE_ID_LIST);
    }

    @Test
    void handle_should_raise_event_and_clear_when_single_valid_id() {

        String validId = "1234567890123456";

        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);
        when(asylumCase.read(CASE_ID_LIST, String.class))
            .thenReturn(Optional.of("  " + validId + "  "));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);

        verify(ccdDataService)
            .raiseEvent(validId, Event.RE_TRIGGER_WA_TASKS);

        verify(asylumCase).clear(CASE_ID_LIST);
    }

    @Test
    void handle_should_raise_only_for_valid_ids_when_multiple_ids() {

        String valid1 = "1111111111111111";
        String invalid = "123";
        String valid2 = "2222222222222222";

        String input = " " + valid1 + " , " + invalid + " , " + valid2 + " ";

        when(callback.getEvent()).thenReturn(Event.RE_TRIGGER_WA_BULK_TASKS);
        when(asylumCase.read(CASE_ID_LIST, String.class))
            .thenReturn(Optional.of(input));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);

        verify(ccdDataService).raiseEvent(valid1, Event.RE_TRIGGER_WA_TASKS);
        verify(ccdDataService).raiseEvent(valid2, Event.RE_TRIGGER_WA_TASKS);

        verify(ccdDataService, times(2))
            .raiseEvent(anyString(), eq(Event.RE_TRIGGER_WA_TASKS));

        verify(asylumCase).clear(CASE_ID_LIST);
    }
}
