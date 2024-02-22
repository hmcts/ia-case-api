package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SendDecisionAndReasonsAmendedMidEventHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private String testPage = "decisionAndReasonsDocumentUploadPage";

    private SendDecisionAndReasonsAmendedMidEventHandler sendDecisionAndReasonsAmendedMidEventHandler;

    @BeforeEach
    public void setUp() {
        sendDecisionAndReasonsAmendedMidEventHandler = new SendDecisionAndReasonsAmendedMidEventHandler
            ("some-name");
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            when(callback.getPageId()).thenReturn(testPage);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = sendDecisionAndReasonsAmendedMidEventHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.MID_EVENT
                    && callback.getEvent() == Event.UPDATE_TRIBUNAL_DECISION
                    && callback.getPageId().equals("decisionAndReasonsDocumentUploadPage")) {
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

        when(callback.getEvent()).thenReturn(Event.UPDATE_TRIBUNAL_DECISION);
        when(callback.getPageId()).thenReturn(testPage);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(DECISION_AND_REASON_DOC_UPLOAD, Document.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sendDecisionAndReasonsAmendedMidEventHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handle_should_return_error_when_document_is_not_pdf() {

        when(callback.getEvent()).thenReturn(Event.UPDATE_TRIBUNAL_DECISION);
        when(callback.getPageId()).thenReturn(testPage);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        Document decisionAndReasonsDocument = new Document("documentUrl", "binaryUrl", "documentFilename.docx");
        when(asylumCase.read(DECISION_AND_REASON_DOC_UPLOAD, Document.class))
            .thenReturn(Optional.of(decisionAndReasonsDocument));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            sendDecisionAndReasonsAmendedMidEventHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assert (callbackResponse.getErrors()).contains("The Decision and reasons document must be a PDF file");
    }
    @Test
    void handle_should_allow_pdf_document() {
        when(callback.getEvent()).thenReturn(Event.UPDATE_TRIBUNAL_DECISION);
        when(callback.getPageId()).thenReturn(testPage);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("PA/12345/2021"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("TestName"));

        Document decisionAndReasonsDocument = new Document("documentUrl", "binaryUrl", "documentFilename.pdf");
        when(asylumCase.read(DECISION_AND_REASON_DOC_UPLOAD, Document.class))
            .thenReturn(Optional.of(decisionAndReasonsDocument));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            sendDecisionAndReasonsAmendedMidEventHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertTrue(callbackResponse.getErrors().isEmpty());
    }
}