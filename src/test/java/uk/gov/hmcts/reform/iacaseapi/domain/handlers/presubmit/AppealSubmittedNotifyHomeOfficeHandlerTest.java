package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppealSubmittedNotifyHomeOfficeHandlerTest {

    private static final String VALID_GWF = "GWF123456789";
    private static final String APPEAL_REF = "PA/12345/2025";

    @Mock
    private HomeOfficeApi<AsylumCase> homeOfficeApi;

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    private AppealSubmittedNotifyHomeOfficeHandler handler;

    @BeforeEach
    void setup() {

        handler = new AppealSubmittedNotifyHomeOfficeHandler(true, homeOfficeApi);

        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(caseDetails.getId()).thenReturn(12345L);
    }

    @Test
    void getDispatchPriority_should_return_last() {

        assertEquals(DispatchPriority.LAST, handler.getDispatchPriority());
    }

    @Test
    void canHandle_should_return_true_for_appeal_started() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_should_return_true_for_appeal_started_by_admin() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED_BY_ADMIN);

        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_should_return_false_for_wrong_stage() {

        assertFalse(handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback));
    }

    @Test
    void canHandle_should_return_false_for_wrong_event() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_should_throw_when_callback_stage_is_null() {

        assertThrows(NullPointerException.class, () -> handler.canHandle(null, callback));
    }

    @Test
    void canHandle_should_throw_when_callback_is_null() {

        assertThrows(NullPointerException.class, () ->
            handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null));
    }

    @Test
    void handle_should_throw_when_cannot_handle() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertThrows(IllegalStateException.class, () ->
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void handle_should_return_without_calling_home_office_when_serialised_data_missing() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(HAS_BEEN_VALIDATED_BY_NEW_HOME_OFFICE_API, YesOrNo.class))
            .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verifyNoInteractions(homeOfficeApi);

        assertEquals(asylumCase, response.getData());
    }

    @Test
    void handle_should_throw_when_home_office_and_gwf_references_missing() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(HAS_BEEN_VALIDATED_BY_NEW_HOME_OFFICE_API, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));

        assertEquals("homeOfficeReferenceNumber and gwfReferenceNumber are both missing - one or other is needed",
            ex.getMessage());
    }

    @Test
    void handle_should_throw_when_appeal_reference_missing() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(HAS_BEEN_VALIDATED_BY_NEW_HOME_OFFICE_API, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(VALID_GWF));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));

        assertEquals("Case ID for the appeal is not present", ex.getMessage());
    }

    @Test
    void handle_should_notify_home_office_and_return_updated_case() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(HAS_BEEN_VALIDATED_BY_NEW_HOME_OFFICE_API, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(VALID_GWF));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(APPEAL_REF));

        when(asylumCase.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, String.class))
            .thenReturn(Optional.of("OK"));

        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(homeOfficeApi).aboutToSubmit(callback);

        assertEquals(asylumCase, response.getData());
    }

    @Test
    void handle_should_use_gwf_reference_when_home_office_reference_missing() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);

        when(asylumCase.read(HAS_BEEN_VALIDATED_BY_NEW_HOME_OFFICE_API, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(VALID_GWF));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(APPEAL_REF));

        when(asylumCase.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, String.class))
            .thenReturn(Optional.of("OK"));

        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(homeOfficeApi).aboutToSubmit(callback);

        assertEquals(asylumCase, response.getData());
    }
}