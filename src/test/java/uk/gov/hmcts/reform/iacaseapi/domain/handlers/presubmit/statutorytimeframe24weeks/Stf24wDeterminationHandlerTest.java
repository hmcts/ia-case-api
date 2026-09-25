package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class Stf24wDeterminationHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AsylumCase updatedAsylumCase;
    @Mock
    private UpdateStatutoryTimeframe24WeeksService updateStatutoryTimeframe24WeeksService;

    private final MockedStatic<HandlerUtils> handlerUtilsMock = mockStatic(HandlerUtils.class);

    private Stf24wDeterminationHandler handler;

    @BeforeEach
    public void setUp() {
        handler = new Stf24wDeterminationHandler("2023-01-01", updateStatutoryTimeframe24WeeksService);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(1234L);
        when(callback.getEvent()).thenReturn(Event.STF_24W_DETERMINATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @AfterEach
    public void tearDown() {
        handlerUtilsMock.close();
    }

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    void should_update_from_determination_if_status(YesOrNo status) {
        when(asylumCase.read(AsylumCaseFieldDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
            .thenReturn(Optional.of(status));
        when(updateStatutoryTimeframe24WeeksService.updateAsylumCaseFromDetermination(asylumCase, status))
            .thenReturn(updatedAsylumCase);

        handlerUtilsMock.when(() -> HandlerUtils.handle24wValidity(eq(callback), any(LocalDate.class)))
            .thenReturn(new PreSubmitCallbackResponse<>(asylumCase));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(updatedAsylumCase, callbackResponse.getData());
        assertNotEquals(asylumCase, callbackResponse.getData());
        assertTrue(callbackResponse.getErrors().isEmpty());
        handlerUtilsMock.verify(() -> HandlerUtils.handle24wValidity(eq(callback), any(LocalDate.class)));
        verify(updateStatutoryTimeframe24WeeksService).updateAsylumCaseFromDetermination(asylumCase, status);
    }

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    void should_not_update_from_determination_if_validity_fails(YesOrNo status) {
        when(asylumCase.read(AsylumCaseFieldDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
            .thenReturn(Optional.of(status));
        when(updateStatutoryTimeframe24WeeksService.updateAsylumCaseFromDetermination(asylumCase, status))
            .thenReturn(updatedAsylumCase);
        handlerUtilsMock.when(() -> HandlerUtils.handle24wValidity(eq(callback), any(LocalDate.class)))
            .thenReturn(new PreSubmitCallbackResponse<>(asylumCase).withError("some error"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotEquals(updatedAsylumCase, callbackResponse.getData());
        assertEquals(asylumCase, callbackResponse.getData());
        assertFalse(callbackResponse.getErrors().isEmpty());
        handlerUtilsMock.verify(() -> HandlerUtils.handle24wValidity(eq(callback), any(LocalDate.class)));
        verify(updateStatutoryTimeframe24WeeksService, never()).updateAsylumCaseFromDetermination(asylumCase, status);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }


    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"STF_24W_DETERMINATION"}, mode = EnumSource.Mode.EXCLUDE)
    void it_cannot_handle_callback_incorrect_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_SUBMIT"}, mode = EnumSource.Mode.EXCLUDE)
    void it_cannot_handle_callback_incorrect_stage(PreSubmitCallbackStage stage) {
        when(callback.getEvent()).thenReturn(Event.STF_24W_DETERMINATION);
        assertFalse(handler.canHandle(stage, callback));
    }

    @Test
    void cannot_handle_callback_no_status() {
        when(callback.getEvent()).thenReturn(Event.STF_24W_DETERMINATION);
        when(asylumCase.read(AsylumCaseFieldDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.empty());
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    void can_handle_callback(YesOrNo status) {
        when(callback.getEvent()).thenReturn(Event.STF_24W_DETERMINATION);
        when(asylumCase.read(AsylumCaseFieldDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(status));
        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
