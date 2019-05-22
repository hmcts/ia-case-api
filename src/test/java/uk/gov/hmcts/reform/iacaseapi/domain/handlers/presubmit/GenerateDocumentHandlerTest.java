package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentGenerator;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class GenerateDocumentHandlerTest {

    @Mock private DocumentGenerator<CaseDataMap> documentGenerator;
    @Mock private Callback<CaseDataMap> callback;

    private GenerateDocumentHandler generateDocumentHandler;

    @Before
    public void setUp() {

        generateDocumentHandler =
            new GenerateDocumentHandler(
                true,
                true,
                documentGenerator
            );
    }

    @Test
    public void should_generate_document_and_update_the_case() {

        Arrays.asList(
            Event.SUBMIT_APPEAL,
            Event.LIST_CASE,
            Event.GENERATE_HEARING_BUNDLE
        ).forEach(event -> {

            CaseDataMap expectedUpdatedCase = mock(CaseDataMap.class);

            when(callback.getEvent()).thenReturn(event);
            when(documentGenerator.generate(callback)).thenReturn(expectedUpdatedCase);

            PreSubmitCallbackResponse<CaseDataMap> callbackResponse =
                generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(expectedUpdatedCase, callbackResponse.getData());

            verify(documentGenerator, times(1)).generate(callback);

            reset(callback);
            reset(documentGenerator);
        });
    }

    @Test
    public void should_be_handled_at_latest_point() {
        assertEquals(DispatchPriority.LATEST, generateDocumentHandler.getDispatchPriority());
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = generateDocumentHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    &&
                    Arrays.asList(
                        Event.SUBMIT_APPEAL,
                        Event.LIST_CASE,
                        Event.GENERATE_HEARING_BUNDLE
                    ).contains(event)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse("failed callback: " + callbackStage + ", failed event " + event, canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void it_cannot_handle_callback_if_docmosis_not_enabled() {

        generateDocumentHandler =
            new GenerateDocumentHandler(
                false,
                true,
                documentGenerator
            );

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = generateDocumentHandler.canHandle(callbackStage, callback);

                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void it_cannot_handle_generate_if_em_stitching_not_enabled() {

        generateDocumentHandler =
            new GenerateDocumentHandler(
                true,
                false,
                documentGenerator
            );

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = generateDocumentHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (event == Event.SUBMIT_APPEAL || event == Event.LIST_CASE)) {
                    assertTrue(canHandle);
                } else if (event == Event.GENERATE_HEARING_BUNDLE) {
                    assertFalse(canHandle);
                } else {
                    assertFalse("event: " + event + ", stage: " + callbackStage, canHandle);
                }

            }

            reset(callback);
        }

    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> generateDocumentHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateDocumentHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateDocumentHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
