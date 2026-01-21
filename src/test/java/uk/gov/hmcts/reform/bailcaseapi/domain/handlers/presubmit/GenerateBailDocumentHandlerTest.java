package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentGenerator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class GenerateBailDocumentHandlerTest {

    @Mock
    private DocumentGenerator<BailCase> documentGenerator;

    @Mock
    private Callback<BailCase> callback;

    private uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.GenerateBailDocumentHandler generateBailDocumentHandler;

    @BeforeEach
    public void setUp() {
        generateBailDocumentHandler = new uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.GenerateBailDocumentHandler(
            documentGenerator
        );
        ReflectionTestUtils.setField(generateBailDocumentHandler, "isDocumentGenerationEnabled", true);
    }

    @Test
    public void should_only_handle_valid_event_state() {
        for (Event event: Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage: PreSubmitCallbackStage.values()) {
                boolean canHandle = generateBailDocumentHandler.canHandle(stage, callback);
                if (stage.equals(PreSubmitCallbackStage.ABOUT_TO_SUBMIT) && Arrays.asList(
                    Event.SUBMIT_APPLICATION,
                    Event.RECORD_THE_DECISION,
                    Event.END_APPLICATION,
                    Event.MAKE_NEW_APPLICATION,
                    Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT,
                    Event.UPLOAD_SIGNED_DECISION_NOTICE,
                    Event.CASE_LISTING
                ).contains(event)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    public void handling_should_throw_if_cannot_handle() {
        assertThatThrownBy(() -> generateBailDocumentHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        assertThatThrownBy(() -> generateBailDocumentHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_START,
            callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    public void should_throw_if_null_args() {
        assertThatThrownBy(() -> generateBailDocumentHandler.canHandle(
            null,
            callback
        ))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> generateBailDocumentHandler.canHandle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            null
        ))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");

        assertThatThrownBy(() -> generateBailDocumentHandler.handle(
            null,
            callback
        ))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> generateBailDocumentHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            null
        ))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    public void should_handle_generate_document_update_bailcase() {
        BailCase expectedBailCase = mock(BailCase.class);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        when(documentGenerator.generate(callback)).thenReturn(expectedBailCase);

        PreSubmitCallbackResponse response = generateBailDocumentHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        assertNotNull(response);
        assertEquals(expectedBailCase, response.getData());
        verify(documentGenerator, times(1)).generate(callback);
    }

    @Test
    public void should_handle_generate_document_signed_decision_notice_upload() {
        BailCase expectedBailCase = mock(BailCase.class);
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(documentGenerator.generate(callback)).thenReturn(expectedBailCase);

        PreSubmitCallbackResponse response = generateBailDocumentHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        assertNotNull(response);
        assertEquals(expectedBailCase, response.getData());
        verify(documentGenerator, times(1)).generate(callback);
    }

    @Test
    public void should_handle_generate_document_end_application() {
        BailCase expectedBailCase = mock(BailCase.class);
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION);
        when(documentGenerator.generate(callback)).thenReturn(expectedBailCase);

        PreSubmitCallbackResponse response = generateBailDocumentHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        assertNotNull(response);
        assertEquals(expectedBailCase, response.getData());
        verify(documentGenerator, times(1)).generate(callback);
    }

    @Test
    public void should_handle_generate_document_make_new_application() {
        BailCase expectedBailCase = mock(BailCase.class);
        when(callback.getEvent()).thenReturn(Event.MAKE_NEW_APPLICATION);
        when(documentGenerator.generate(callback)).thenReturn(expectedBailCase);

        PreSubmitCallbackResponse response = generateBailDocumentHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        assertNotNull(response);
        assertEquals(expectedBailCase, response.getData());
        verify(documentGenerator, times(1)).generate(callback);
    }

    @Test
    public void should_handle_relist_case_listing_event() {
        BailCase expectedBailCase = mock(BailCase.class);
        when(callback.getEvent()).thenReturn(Event.CASE_LISTING);
        when(documentGenerator.generate(callback)).thenReturn(expectedBailCase);

        PreSubmitCallbackResponse response = generateBailDocumentHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        assertNotNull(response);
        assertEquals(expectedBailCase, response.getData());
        verify(documentGenerator, times(1)).generate(callback);
    }
}
