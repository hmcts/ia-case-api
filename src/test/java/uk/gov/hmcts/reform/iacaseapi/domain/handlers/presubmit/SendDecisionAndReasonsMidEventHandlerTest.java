package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FINAL_DECISION_AND_REASONS_DOCUMENT;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SendDecisionAndReasonsMidEventHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private SendDecisionAndReasonsMidEventHandler sendDecisionAndReasonsMidEventHandler;

    @BeforeEach
    public void setUp() {
        sendDecisionAndReasonsMidEventHandler = new SendDecisionAndReasonsMidEventHandler();
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = sendDecisionAndReasonsMidEventHandler.canHandle(callbackStage, callback);

                if (event == Event.SEND_DECISION_AND_REASONS
                        && callbackStage == PreSubmitCallbackStage.MID_EVENT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void handle_throw_exception_when_document_is_null() {

        when(callback.getEvent()).thenReturn(Event.SEND_DECISION_AND_REASONS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENT, Document.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sendDecisionAndReasonsMidEventHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
                .hasMessage("finalDecisionAndReasonsDocument must be present")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handle_should_return_error_when_document_is_not_pdf() {

        when(callback.getEvent()).thenReturn(Event.SEND_DECISION_AND_REASONS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        Document decisionAndReasonsDocument = new Document("documentUrl", "binaryUrl", "documentFilename.docx");
        when(asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENT, Document.class))
                .thenReturn(Optional.of(decisionAndReasonsDocument));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                sendDecisionAndReasonsMidEventHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assert (callbackResponse.getErrors()).contains("The Decision and reasons document must be a PDF file");
    }

    @Test
    void handle_should_allow_pdf_document() {
        when(callback.getEvent()).thenReturn(Event.SEND_DECISION_AND_REASONS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        Document decisionAndReasonsDocument = new Document("documentUrl", "binaryUrl", "documentFilename.pdf");
        when(asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENT, Document.class))
                .thenReturn(Optional.of(decisionAndReasonsDocument));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                sendDecisionAndReasonsMidEventHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertTrue(callbackResponse.getErrors().isEmpty());
    }
}