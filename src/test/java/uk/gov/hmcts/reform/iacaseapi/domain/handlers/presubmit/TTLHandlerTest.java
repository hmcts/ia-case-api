package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.TTL;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TTLHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private TTL ttl;

    private TTLHandler ttlHandler;

    @BeforeEach
    public void setUp() {
        ttlHandler = new TTLHandler();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_get_dispatch_priority() {
        assertEquals(DispatchPriority.EARLY, ttlHandler.getDispatchPriority());
    }

    @Test
    void should_handle_callback_stage_and_event() {
        List<Event> validEvents = List.of(SEND_DECISION_AND_REASONS, END_APPEAL, REMOVE_APPEAL_FROM_ONLINE, APPLY_FOR_FTPA_APPELLANT);

        for (Event event : validEvents) {
            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = ttlHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertTrue(canHandle);
        }
    }

    @Test
    void should_not_handle_other_callback_stage_or_event() {
        when(callback.getEvent()).thenReturn(START_APPEAL);

        boolean canHandle = ttlHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);

        canHandle = ttlHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_throw_exception_when_cannot_handle() {
        when(callback.getEvent()).thenReturn(START_APPEAL);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            ttlHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)
        );

        assertEquals("Cannot handle callback", exception.getMessage());
    }

    @Test
    void should_clear_ttl_when_event_is_reinstate_appeal() {
        when(callback.getEvent()).thenReturn(REINSTATE_APPEAL);
        when(asylumCase.read(AsylumCaseFieldDefinition.TTL, TTL.class)).thenReturn(Optional.of(ttl));

        PreSubmitCallbackResponse<AsylumCase> response = ttlHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).clear(AsylumCaseFieldDefinition.TTL);
        assertEquals(asylumCase, response.getData());
    }

    @Test
    void should_clear_override_ttl_when_ttl_is_present() {
        when(callback.getEvent()).thenReturn(SEND_DECISION_AND_REASONS);
        when(asylumCase.read(AsylumCaseFieldDefinition.TTL, TTL.class)).thenReturn(Optional.of(ttl));

        PreSubmitCallbackResponse<AsylumCase> response = ttlHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(ttl).setOverrideTTL(null);
        assertEquals(asylumCase, response.getData());
    }

    @Test
    void should_not_change_ttl_when_ttl_is_not_present() {
        when(callback.getEvent()).thenReturn(SEND_DECISION_AND_REASONS);
        when(asylumCase.read(AsylumCaseFieldDefinition.TTL, TTL.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response = ttlHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, never()).clear(AsylumCaseFieldDefinition.TTL);
        assertEquals(asylumCase, response.getData());
    }

    @Test
    void should_require_non_null_parameters_in_canHandle() {
        NullPointerException exception = assertThrows(NullPointerException.class, () ->
            ttlHandler.canHandle(null, callback)
        );
        assertEquals("callbackStage must not be null", exception.getMessage());

        exception = assertThrows(NullPointerException.class, () ->
            ttlHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null)
        );
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void should_require_non_null_parameters_in_handle() {
        NullPointerException exception = assertThrows(NullPointerException.class, () ->
            ttlHandler.handle(null, callback)
        );
        assertEquals("callbackStage must not be null", exception.getMessage());

        exception = assertThrows(NullPointerException.class, () ->
            ttlHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null)
        );
        assertEquals("callback must not be null", exception.getMessage());
    }
}
