package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UpdateTribunalDecisionDocumentUploadRule31MidEventTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private String testPage = "decisionAndReasonsDocumentUploadPage";

    private UpdateTribunalDecisionDocumentUploadRule31MidEvent updateTribunalDecisionDocumentUploadRule31MidEvent;

    @BeforeEach
    public void setUp() {
        updateTribunalDecisionDocumentUploadRule31MidEvent = new UpdateTribunalDecisionDocumentUploadRule31MidEvent();

        when(callback.getEvent()).thenReturn(Event.UPDATE_TRIBUNAL_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, String.class))
                .thenReturn(Optional.of("underRule31"));
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            when(callback.getPageId()).thenReturn(testPage);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = updateTribunalDecisionDocumentUploadRule31MidEvent.canHandle(callbackStage, callback);

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
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> updateTribunalDecisionDocumentUploadRule31MidEvent.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> updateTribunalDecisionDocumentUploadRule31MidEvent.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> updateTribunalDecisionDocumentUploadRule31MidEvent.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateTribunalDecisionDocumentUploadRule31MidEvent.canHandle(PreSubmitCallbackStage.MID_EVENT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateTribunalDecisionDocumentUploadRule31MidEvent.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateTribunalDecisionDocumentUploadRule31MidEvent.handle(PreSubmitCallbackStage.MID_EVENT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handle_should_return_error_when_document_is_not_pdf() {

        when(callback.getEvent()).thenReturn(Event.UPDATE_TRIBUNAL_DECISION);
        when(callback.getPageId()).thenReturn(testPage);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        Document decisionAndReasonsDocument = new Document("documentUrl", "binaryUrl", "documentFilename.docx");
        when(asylumCase.read(DECISION_AND_REASON_DOCS_UPLOAD, Document.class))
            .thenReturn(Optional.of(decisionAndReasonsDocument));

        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                updateTribunalDecisionDocumentUploadRule31MidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assert (callbackResponse.getErrors()).contains("The Decision and reasons document must be a PDF file");
    }

    @Test
    void handle_should_return_error_when_both_selections_are_no() {

        when(callback.getEvent()).thenReturn(Event.UPDATE_TRIBUNAL_DECISION);
        when(callback.getPageId()).thenReturn(testPage);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        DynamicList dynamicList = new DynamicList(new Value("dismissed", "No"),
                List.of(
                        new Value("allowed", "Yes, change decision to Allowed"),
                        new Value("dismissed", "No")));

        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicList));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                updateTribunalDecisionDocumentUploadRule31MidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assert (callbackResponse.getErrors()).contains("You must update the decision or the Decision and Reasons document to continue.");
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
        when(asylumCase.read(DECISION_AND_REASON_DOCS_UPLOAD, Document.class))
            .thenReturn(Optional.of(decisionAndReasonsDocument));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                updateTribunalDecisionDocumentUploadRule31MidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertTrue(callbackResponse.getErrors().isEmpty());
    }
}