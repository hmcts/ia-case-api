package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority.EARLIEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class BailReferenceNumberHandlerTest {

    @Mock
    private Callback<BailCase> callback;

    @Mock
    private BailCase bailCase;

    @Mock
    private CaseDetails<BailCase> caseDetails;

    private BailReferenceNumberHandler bailReferenceNumberHandler;

    @BeforeEach
    public void setUp() {
        bailReferenceNumberHandler = new BailReferenceNumberHandler();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
    }

    @Test
    void set_to_earliest() {
        assertThat(bailReferenceNumberHandler.getDispatchPriority()).isEqualTo(EARLIEST);
    }

    @Test
    void set_formatted_bail_reference_number_if_empty() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(""));
        when(callback.getCaseDetails().getId()).thenReturn(Long.valueOf("9001900290039004"));

        PreSubmitCallbackResponse<BailCase> response = bailReferenceNumberHandler
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1))
            .write(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, "9001-9002-9003-9004");

    }

    @Test
    void set_formatted_bail_reference_number_if_Draft() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER)).thenReturn(Optional.of("DRAFT"));
        when(callback.getCaseDetails().getId()).thenReturn(Long.valueOf("9001900290039004"));

        PreSubmitCallbackResponse<BailCase> response = bailReferenceNumberHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1))
            .write(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, "9001-9002-9003-9004");

    }

    @Test
    void should_do_nothing_if_bail_reference_already_present() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER)).thenReturn(Optional.of("9001-9002-9003-9004"));

        PreSubmitCallbackResponse<BailCase> response = bailReferenceNumberHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, never()).write(any(), any());

    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = bailReferenceNumberHandler.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_SUBMIT && (callback.getEvent() == Event.START_APPLICATION)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {
        //invalid stage
        assertThatThrownBy(() -> bailReferenceNumberHandler.handle(ABOUT_TO_START, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);

        //invalid event
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION);
        assertThatThrownBy(() -> bailReferenceNumberHandler.handle(ABOUT_TO_SUBMIT, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> bailReferenceNumberHandler.canHandle(null, callback)).hasMessage(
            "callbackStage must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> bailReferenceNumberHandler.canHandle(ABOUT_TO_SUBMIT, null)).hasMessage(
            "callback must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> bailReferenceNumberHandler.handle(null, callback)).hasMessage(
            "callbackStage must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> bailReferenceNumberHandler.handle(ABOUT_TO_SUBMIT, null)).hasMessage(
            "callback must not be null").isExactlyInstanceOf(NullPointerException.class);
    }

}
