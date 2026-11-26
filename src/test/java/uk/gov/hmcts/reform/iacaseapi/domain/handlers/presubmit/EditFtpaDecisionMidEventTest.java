package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_DECISION_AND_REASONS_DOC_UPLOAD;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CurrentFtpaDecisionResolver;

@ExtendWith(MockitoExtension.class)
class EditFtpaDecisionMidEventTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CurrentFtpaDecisionResolver resolver;

    private EditFtpaDecisionMidEvent midEvent;

    @BeforeEach
    void setUp() {
        midEvent = new EditFtpaDecisionMidEvent(resolver);
    }

    private void givenValidCallback() {
        when(callback.getEvent()).thenReturn(Event.UPDATE_FTPA_DECISION);
        when(callback.getPageId()).thenReturn(EditFtpaDecisionMidEvent.DOCUMENT_UPLOAD_PAGE_ID);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_error_when_nothing_changed() {
        givenValidCallback();
        when(resolver.isDecisionChanged(asylumCase)).thenReturn(false);
        when(resolver.isDocumentUploaded(asylumCase)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> response =
            midEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(response.getErrors()).containsExactly(EditFtpaDecisionMidEvent.NOTHING_CHANGED_ERROR);
    }

    @Test
    void should_pass_when_only_decision_changed() {
        givenValidCallback();
        when(resolver.isDecisionChanged(asylumCase)).thenReturn(true);
        when(resolver.isDocumentUploaded(asylumCase)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> response =
            midEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(response.getErrors()).isEmpty();
        verify(asylumCase, never()).write(eq(FTPA_DECISION_AND_REASONS_DOC_UPLOAD), any());
    }

    @Test
    void should_error_when_uploaded_document_is_not_pdf() {
        givenValidCallback();
        when(resolver.isDocumentUploaded(asylumCase)).thenReturn(true);
        when(asylumCase.read(FTPA_DECISION_AND_REASONS_DOC_UPLOAD, Document.class))
            .thenReturn(Optional.of(new Document("url", "binaryUrl", "decision.docx")));

        PreSubmitCallbackResponse<AsylumCase> response =
            midEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(response.getErrors()).containsExactly(EditFtpaDecisionMidEvent.NOT_PDF_ERROR);
    }

    @Test
    void should_rename_uploaded_pdf() {
        givenValidCallback();
        when(resolver.isDocumentUploaded(asylumCase)).thenReturn(true);
        when(asylumCase.read(FTPA_DECISION_AND_REASONS_DOC_UPLOAD, Document.class))
            .thenReturn(Optional.of(new Document("url", "binaryUrl", "decision.PDF")));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("PA/12345/2024"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Smith"));

        PreSubmitCallbackResponse<AsylumCase> response =
            midEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(response.getErrors()).isEmpty();
        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(asylumCase).write(eq(FTPA_DECISION_AND_REASONS_DOC_UPLOAD), captor.capture());
        assertThat(captor.getValue().getDocumentUrl()).isEqualTo("url");
        assertThat(captor.getValue().getDocumentFilename())
            .isEqualTo("PA 12345 2024-Smith-Decision-and-reasons-UPDATED.pdf");
    }

    @Test
    void can_handle_only_mid_event_on_upload_page() {
        when(callback.getEvent()).thenReturn(Event.UPDATE_FTPA_DECISION);
        when(callback.getPageId()).thenReturn(EditFtpaDecisionMidEvent.DOCUMENT_UPLOAD_PAGE_ID);
        assertThat(midEvent.canHandle(PreSubmitCallbackStage.MID_EVENT, callback)).isTrue();
        assertThat(midEvent.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)).isFalse();

        when(callback.getPageId()).thenReturn("otherPage");
        assertThat(midEvent.canHandle(PreSubmitCallbackStage.MID_EVENT, callback)).isFalse();

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThat(midEvent.canHandle(PreSubmitCallbackStage.MID_EVENT, callback)).isFalse();
    }

    @Test
    void should_throw_if_cannot_handle() {
        assertThatThrownBy(() -> midEvent.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> midEvent.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> midEvent.canHandle(PreSubmitCallbackStage.MID_EVENT, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
