package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_B1_FORM_DOCS;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UploadB1DocumentHandlerTest {

    @Mock
    private DocumentReceiver documentReceiver;
    @Mock
    private DocumentsAppender documentsAppender;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private DocumentWithDescription b1Document;
    @Mock
    private DocumentWithMetadata b1Document1WithMetadata;
    @Mock
    private List<IdValue<DocumentWithMetadata>> existingApplicantDocuments;
    @Mock
    private List<IdValue<DocumentWithMetadata>> allApplicantDocuments;

    @Captor
    private ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> existingApplicantDocumentsCaptor;

    private UploadB1DocumentHandler uploadB1DocumentHandler;

    @BeforeEach
    public void setUp() {
        uploadB1DocumentHandler =
            new UploadB1DocumentHandler(
                documentReceiver,
                documentsAppender
            );
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
    }

    @Test
    void should_be_handled_at_latest_point() {
        assertEquals(DispatchPriority.LATEST, uploadB1DocumentHandler.getDispatchPriority());
    }

    @Test
    void should_append_b1Document_to_existing_applicant_documents_for_the_case() {
        when(bailCase.read(APPLICANT_DOCUMENTS_WITH_METADATA)).thenReturn(Optional.of(existingApplicantDocuments));

        List<DocumentWithMetadata> b1DocumentWithMetadataList =
            Arrays.asList(
                b1Document1WithMetadata
            );

        List<IdValue<DocumentWithDescription>> b1DocumentList =
            Arrays.asList(
                new IdValue<>("1", b1Document)
            );

        when(bailCase.read(UPLOAD_B1_FORM_DOCS)).thenReturn(Optional.of(b1DocumentList));

        when(documentReceiver.tryReceive(b1Document, DocumentTag.B1_DOCUMENT))
            .thenReturn(Optional.of(b1Document1WithMetadata));

        when(documentsAppender.append(existingApplicantDocuments, b1DocumentWithMetadataList))
            .thenReturn(allApplicantDocuments);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            uploadB1DocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).read(UPLOAD_B1_FORM_DOCS);

        verify(documentReceiver, times(1)).tryReceive(b1Document, DocumentTag.B1_DOCUMENT);
        verify(documentsAppender, times(1)).append(existingApplicantDocuments, b1DocumentWithMetadataList);
        verify(bailCase, times(1))
            .write(APPLICANT_DOCUMENTS_WITH_METADATA, allApplicantDocuments);
    }

    @Test
    void should_add_b1Document_to_the_case_when_no_applicant_documents_exist() {

        List<IdValue<DocumentWithDescription>> b1DocumentWithDescriptionList =
            singletonList(new IdValue<>("1", b1Document));
        List<DocumentWithMetadata> b1DocumentWithMetadata = singletonList(b1Document1WithMetadata);

        when(bailCase.read(APPLICANT_DOCUMENTS_WITH_METADATA)).thenReturn(Optional.empty());
        when(bailCase.read(UPLOAD_B1_FORM_DOCS)).thenReturn(Optional.of(b1DocumentWithDescriptionList));

        when(documentReceiver.tryReceive(b1Document, DocumentTag.B1_DOCUMENT))
            .thenReturn(Optional.of(b1Document1WithMetadata));

        when(documentsAppender.append(any(List.class), eq(b1DocumentWithMetadata)))
            .thenReturn(allApplicantDocuments);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            uploadB1DocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).read(UPLOAD_B1_FORM_DOCS);

        verify(documentReceiver, times(1)).tryReceive(b1Document, DocumentTag.B1_DOCUMENT);

        verify(documentsAppender, times(1))
            .append(existingApplicantDocumentsCaptor.capture(), eq(b1DocumentWithMetadata));

        List<IdValue<DocumentWithMetadata>> actualExistingApplicantDocuments =
            existingApplicantDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, actualExistingApplicantDocuments.size());

        verify(bailCase, times(1))
            .write(APPLICANT_DOCUMENTS_WITH_METADATA, allApplicantDocuments);
    }

    @Test
    void should_not_change_application_documents_if_b1Document_is_not_present() {

        when(bailCase.read(UPLOAD_B1_FORM_DOCS)).thenReturn(Optional.empty());
        when(callback.getCaseDetails().getCaseData()).thenReturn(bailCase);

        assertDoesNotThrow(() -> uploadB1DocumentHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
        PreSubmitCallbackResponse<BailCase> response = uploadB1DocumentHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(bailCase, response.getData());
    }

    @Test
    public void should_only_handle_valid_event_state() {
        for (Event event: Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage: PreSubmitCallbackStage.values()) {
                boolean canHandle = uploadB1DocumentHandler.canHandle(stage, callback);
                if (stage.equals(PreSubmitCallbackStage.ABOUT_TO_SUBMIT) && Arrays.asList(
                    Event.SUBMIT_APPLICATION,
                    Event.MAKE_NEW_APPLICATION,
                    Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT
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
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> uploadB1DocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> uploadB1DocumentHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> uploadB1DocumentHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadB1DocumentHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadB1DocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
