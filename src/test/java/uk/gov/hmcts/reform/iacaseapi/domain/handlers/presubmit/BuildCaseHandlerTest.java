package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_ARGUMENT_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_ARGUMENT_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_ARGUMENT_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BuildCaseHandlerTest {

    @Mock
    private DocumentReceiver documentReceiver;
    @Mock
    private DocumentsAppender documentsAppender;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Document caseArgumentDocument;
    private String caseArgumentDescription = "Case argument description";
    @Mock
    private DocumentWithMetadata caseArgumentWithMetadata;
    @Mock
    private DocumentWithDescription caseArgumentEvidence1;
    @Mock
    private DocumentWithDescription caseArgumentEvidence2;
    @Mock
    private DocumentWithMetadata caseArgumentEvidence1WithMetadata;
    @Mock
    private DocumentWithMetadata caseArgumentEvidence2WithMetadata;
    @Mock
    private List<IdValue<DocumentWithMetadata>> existingLegalRepresentativeDocuments;
    @Mock
    private List<IdValue<DocumentWithMetadata>> allLegalRepresentativeDocuments;

    @Captor
    private ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> legalRepresentativeDocumentsCaptor;

    private BuildCaseHandler buildCaseHandler;

    @BeforeEach
    public void setUp() {
        buildCaseHandler =
            new BuildCaseHandler(
                documentReceiver,
                documentsAppender
            );
    }

    @Test
    void should_append_case_argument_to_legal_representative_documents_for_the_case() {

        List<IdValue<DocumentWithDescription>> caseArgumentEvidence =
            Arrays.asList(
                new IdValue<>("1", caseArgumentEvidence1),
                new IdValue<>("2", caseArgumentEvidence2)
            );

        List<DocumentWithMetadata> caseArgumentEvidenceWithMetadata =
            Arrays.asList(
                caseArgumentEvidence1WithMetadata,
                caseArgumentEvidence2WithMetadata
            );

        List<DocumentWithMetadata> caseArgumentDocumentsWithMetadata =
            Arrays.asList(
                caseArgumentWithMetadata,
                caseArgumentEvidence1WithMetadata,
                caseArgumentEvidence2WithMetadata
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS))
            .thenReturn(Optional.of(existingLegalRepresentativeDocuments));
        when(asylumCase.read(CASE_ARGUMENT_DOCUMENT, Document.class)).thenReturn(Optional.of(caseArgumentDocument));
        when(asylumCase.read(CASE_ARGUMENT_DESCRIPTION, String.class)).thenReturn(Optional.of(caseArgumentDescription));
        when(asylumCase.read(CASE_ARGUMENT_EVIDENCE)).thenReturn(Optional.of(caseArgumentEvidence));

        when(documentReceiver.receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT))
            .thenReturn(caseArgumentWithMetadata);

        when(documentReceiver.tryReceiveAll(caseArgumentEvidence, DocumentTag.CASE_ARGUMENT))
            .thenReturn(caseArgumentEvidenceWithMetadata);

        when(documentsAppender
            .append(existingLegalRepresentativeDocuments, caseArgumentDocumentsWithMetadata))
            .thenReturn(allLegalRepresentativeDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            buildCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(CASE_ARGUMENT_DOCUMENT, Document.class);
        verify(asylumCase, times(1)).read(CASE_ARGUMENT_DESCRIPTION, String.class);
        verify(asylumCase, times(1)).read(CASE_ARGUMENT_EVIDENCE);

        verify(documentReceiver, times(1))
            .receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT);
        verify(documentReceiver, times(1)).tryReceiveAll(caseArgumentEvidence, DocumentTag.CASE_ARGUMENT);

        verify(documentsAppender, times(1))
            .append(
                existingLegalRepresentativeDocuments,
                caseArgumentDocumentsWithMetadata
            );

        verify(asylumCase, times(1)).write(LEGAL_REPRESENTATIVE_DOCUMENTS, allLegalRepresentativeDocuments);

        verify(asylumCase, times(1)).clear(CASE_ARGUMENT_DOCUMENT);
        verify(asylumCase, times(1)).clear(CASE_ARGUMENT_DESCRIPTION);
        verify(asylumCase, times(1)).clear(CASE_ARGUMENT_EVIDENCE);
    }

    @Test
    void should_add_case_argument_to_the_case_when_no_legal_representative_documents_exist() {

        List<IdValue<DocumentWithDescription>> caseArgumentEvidence =
            Arrays.asList(
                new IdValue<>("1", caseArgumentEvidence1),
                new IdValue<>("2", caseArgumentEvidence2)
            );

        List<DocumentWithMetadata> caseArgumentEvidenceWithMetadata =
            Arrays.asList(
                caseArgumentEvidence1WithMetadata,
                caseArgumentEvidence2WithMetadata
            );

        List<DocumentWithMetadata> caseArgumentDocumentsWithMetadata =
            Arrays.asList(
                caseArgumentWithMetadata,
                caseArgumentEvidence1WithMetadata,
                caseArgumentEvidence2WithMetadata
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.empty());
        when(asylumCase.read(CASE_ARGUMENT_DOCUMENT, Document.class)).thenReturn(Optional.of(caseArgumentDocument));
        when(asylumCase.read(CASE_ARGUMENT_DESCRIPTION, String.class)).thenReturn(Optional.of(caseArgumentDescription));
        when(asylumCase.read(CASE_ARGUMENT_EVIDENCE)).thenReturn(Optional.of(caseArgumentEvidence));

        when(documentReceiver.receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT))
            .thenReturn(caseArgumentWithMetadata);

        when(documentReceiver.tryReceiveAll(caseArgumentEvidence, DocumentTag.CASE_ARGUMENT))
            .thenReturn(caseArgumentEvidenceWithMetadata);

        when(documentsAppender
            .append(any(List.class), eq(caseArgumentDocumentsWithMetadata)))
            .thenReturn(allLegalRepresentativeDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            buildCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(CASE_ARGUMENT_DOCUMENT, Document.class);
        verify(asylumCase, times(1)).read(CASE_ARGUMENT_DESCRIPTION, String.class);
        verify(asylumCase, times(1)).read(CASE_ARGUMENT_EVIDENCE);

        verify(documentReceiver, times(1))
            .receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT);
        verify(documentReceiver, times(1)).tryReceiveAll(caseArgumentEvidence, DocumentTag.CASE_ARGUMENT);

        verify(documentsAppender, times(1))
            .append(
                legalRepresentativeDocumentsCaptor.capture(),
                eq(caseArgumentDocumentsWithMetadata)
            );

        List<IdValue<DocumentWithMetadata>> legalRepresentativeDocuments =
            legalRepresentativeDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, legalRepresentativeDocuments.size());

        verify(asylumCase, times(1)).write(LEGAL_REPRESENTATIVE_DOCUMENTS, allLegalRepresentativeDocuments);
    }

    @Test
    void should_throw_when_case_argument_document_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(CASE_ARGUMENT_DOCUMENT, Document.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> buildCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("caseArgumentDocument is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> buildCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> buildCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = buildCaseHandler.canHandle(callbackStage, callback);

                if (event == Event.BUILD_CASE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> buildCaseHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> buildCaseHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> buildCaseHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> buildCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
