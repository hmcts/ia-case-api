package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class AipNlrEventSubmissionHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private AipNlrEventSubmissionHandler handler;

    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        handler = new AipNlrEventSubmissionHandler();
    }

    @Test
    void canHandle_should_throw_exception_for_null_arguments() {
        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> handler.canHandle(null, callback));
        assertEquals("callbackStage must not be null", exception.getMessage());

        exception =
            assertThrows(NullPointerException.class, () -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_SUBMIT"}, mode = EnumSource.Mode.EXCLUDE)
    void canHandle_should_return_false_for_invalid_stage(PreSubmitCallbackStage callbackStage) {
        assertFalse(handler.canHandle(callbackStage, callback));
    }

    @Test
    void canHandle_should_return_false_for_hasNlrSubmitted_no() {
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(Event.class)
    void canHandle_should_return_true_for_hasNlrSubmitted_yes(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(AsylumCaseFieldDefinition.HAS_NLR_SUBMITTED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void handle_should_throw_exception_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(AsylumCaseFieldDefinition.HAS_NLR_SUBMITTED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
        assertEquals("Cannot handle callback", exception.getMessage());
    }

    @Test
    void handle_should_clear_hasNlrSubmitted() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(AsylumCaseFieldDefinition.HAS_NLR_SUBMITTED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.HAS_NLR_SUBMITTED);
    }
}