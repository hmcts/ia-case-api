package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class BuildCaseHandlerTest {

    @Mock private DocumentReceiver documentReceiver;
    @Mock private DocumentsAppender documentsAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase AsylumCase;
    @Mock private Document caseArgumentDocument;
    private String caseArgumentDescription = "Case argument description";
    @Mock private DocumentWithMetadata caseArgumentWithMetadata;
    @Mock private DocumentWithDescription caseArgumentEvidence1;
    @Mock private DocumentWithDescription caseArgumentEvidence2;
    @Mock private DocumentWithMetadata caseArgumentEvidence1WithMetadata;
    @Mock private DocumentWithMetadata caseArgumentEvidence2WithMetadata;
    @Mock private List<IdValue<DocumentWithMetadata>> existingLegalRepresentativeDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> allLegalRepresentativeDocuments;

    @Captor private ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> legalRepresentativeDocumentsCaptor;

    private BuildCaseHandler buildCaseHandler;

    @Before
    public void setUp() {
        buildCaseHandler =
            new BuildCaseHandler(
                documentReceiver,
                documentsAppender
            );
    }

    @Test
    public void should_append_case_argument_to_legal_representative_documents_for_the_case() {

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
        when(caseDetails.getCaseData()).thenReturn(AsylumCase);
        when(AsylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.of(existingLegalRepresentativeDocuments));
        when(AsylumCase.read(CASE_ARGUMENT_DOCUMENT, Document.class)).thenReturn(Optional.of(caseArgumentDocument));
        when(AsylumCase.read(CASE_ARGUMENT_DESCRIPTION, String.class)).thenReturn(Optional.of(caseArgumentDescription));
        when(AsylumCase.read(CASE_ARGUMENT_EVIDENCE)).thenReturn(Optional.of(caseArgumentEvidence));

        when(documentReceiver.receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT))
            .thenReturn(caseArgumentWithMetadata);

        when(documentReceiver.tryReceiveAll(caseArgumentEvidence, DocumentTag.CASE_ARGUMENT))
            .thenReturn(caseArgumentEvidenceWithMetadata);

        when(documentsAppender.append(existingLegalRepresentativeDocuments, caseArgumentDocumentsWithMetadata, DocumentTag.CASE_ARGUMENT))
            .thenReturn(allLegalRepresentativeDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            buildCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(AsylumCase, callbackResponse.getData());

        verify(AsylumCase, times(1)).read(CASE_ARGUMENT_DOCUMENT, Document.class);
        verify(AsylumCase, times(1)).read(CASE_ARGUMENT_DESCRIPTION, String.class);
        verify(AsylumCase, times(1)).read(CASE_ARGUMENT_EVIDENCE);

        verify(documentReceiver, times(1)).receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT);
        verify(documentReceiver, times(1)).tryReceiveAll(caseArgumentEvidence, DocumentTag.CASE_ARGUMENT);

        verify(documentsAppender, times(1))
            .append(
                existingLegalRepresentativeDocuments,
                caseArgumentDocumentsWithMetadata,
                DocumentTag.CASE_ARGUMENT
            );

        verify(AsylumCase, times(1)).write(LEGAL_REPRESENTATIVE_DOCUMENTS, allLegalRepresentativeDocuments);
        verify(AsylumCase, times(1)).write(CASE_ARGUMENT_AVAILABLE, YesOrNo.YES);
    }

    @Test
    public void should_add_case_argument_to_the_case_when_no_legal_representative_documents_exist() {

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
        when(caseDetails.getCaseData()).thenReturn(AsylumCase);
        when(AsylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.empty());
        when(AsylumCase.read(CASE_ARGUMENT_DOCUMENT, Document.class)).thenReturn(Optional.of(caseArgumentDocument));
        when(AsylumCase.read(CASE_ARGUMENT_DESCRIPTION, String.class)).thenReturn(Optional.of(caseArgumentDescription));
        when(AsylumCase.read(CASE_ARGUMENT_EVIDENCE)).thenReturn(Optional.of(caseArgumentEvidence));

        when(documentReceiver.receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT))
            .thenReturn(caseArgumentWithMetadata);

        when(documentReceiver.tryReceiveAll(caseArgumentEvidence, DocumentTag.CASE_ARGUMENT))
            .thenReturn(caseArgumentEvidenceWithMetadata);

        when(documentsAppender.append(any(List.class), eq(caseArgumentDocumentsWithMetadata), eq(DocumentTag.CASE_ARGUMENT)))
            .thenReturn(allLegalRepresentativeDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            buildCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(AsylumCase, callbackResponse.getData());

        verify(AsylumCase, times(1)).read(CASE_ARGUMENT_DOCUMENT, Document.class);
        verify(AsylumCase, times(1)).read(CASE_ARGUMENT_DESCRIPTION, String.class);
        verify(AsylumCase, times(1)).read(CASE_ARGUMENT_EVIDENCE);

        verify(documentReceiver, times(1)).receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT);
        verify(documentReceiver, times(1)).tryReceiveAll(caseArgumentEvidence, DocumentTag.CASE_ARGUMENT);

        verify(documentsAppender, times(1))
            .append(
                legalRepresentativeDocumentsCaptor.capture(),
                eq(caseArgumentDocumentsWithMetadata),
                eq(DocumentTag.CASE_ARGUMENT)
            );

        List<IdValue<DocumentWithMetadata>> legalRepresentativeDocuments =
            legalRepresentativeDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, legalRepresentativeDocuments.size());

        verify(AsylumCase, times(1)).write(LEGAL_REPRESENTATIVE_DOCUMENTS, allLegalRepresentativeDocuments);
        verify(AsylumCase, times(1)).write(CASE_ARGUMENT_AVAILABLE, YesOrNo.YES);
    }

    @Test
    public void should_throw_when_case_argument_document_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(caseDetails.getCaseData()).thenReturn(AsylumCase);

        when(AsylumCase.read(CASE_ARGUMENT_DOCUMENT, Document.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> buildCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("caseArgumentDocument is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> buildCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> buildCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

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
    public void should_not_allow_null_arguments() {

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
