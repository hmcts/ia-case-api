package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class BuildCaseHandlerTest {

    @Mock private DocumentReceiver documentReceiver;
    @Mock private DocumentsAppender documentsAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document caseArgumentDocument;
    private String caseArgumentDescription = "Case argument description";
    @Mock private DocumentWithMetadata caseArgumentWithMetadata;
    @Mock private DocumentWithDescription caseArgumentEvidence1;
    @Mock private DocumentWithDescription caseArgumentEvidence2;
    @Mock private DocumentWithMetadata caseArgumentEvidence1WithMetadata;
    @Mock private DocumentWithMetadata caseArgumentEvidence2WithMetadata;
    @Mock private List<IdValue<DocumentWithMetadata>> existingLegalRepresentativeDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> allLegalRepresentativeDocuments;

    @Captor private ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> filteredLegalRepresentativeDocumentsCaptor;

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

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getLegalRepresentativeDocuments()).thenReturn(Optional.of(existingLegalRepresentativeDocuments));
        when(asylumCase.getCaseArgumentDocument()).thenReturn(Optional.of(caseArgumentDocument));
        when(asylumCase.getCaseArgumentDescription()).thenReturn(Optional.of(caseArgumentDescription));
        when(asylumCase.getCaseArgumentEvidence()).thenReturn(Optional.of(caseArgumentEvidence));

        when(documentReceiver.receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT))
            .thenReturn(Optional.of(caseArgumentWithMetadata));

        when(documentReceiver.receive(caseArgumentEvidence1, DocumentTag.CASE_ARGUMENT))
            .thenReturn(Optional.of(caseArgumentEvidence1WithMetadata));

        when(documentReceiver.receive(caseArgumentEvidence2, DocumentTag.CASE_ARGUMENT))
            .thenReturn(Optional.of(caseArgumentEvidence2WithMetadata));

        when(documentsAppender.append(any(List.class), eq(caseArgumentEvidenceWithMetadata)))
            .thenReturn(allLegalRepresentativeDocuments);

        when(documentsAppender.append(allLegalRepresentativeDocuments, Collections.singletonList(caseArgumentWithMetadata)))
            .thenReturn(allLegalRepresentativeDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            buildCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).getCaseArgumentDocument();
        verify(asylumCase, times(1)).getCaseArgumentDescription();
        verify(asylumCase, times(1)).getCaseArgumentEvidence();

        verify(documentReceiver, times(1)).receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT);
        verify(documentReceiver, times(1)).receive(caseArgumentEvidence1, DocumentTag.CASE_ARGUMENT);
        verify(documentReceiver, times(1)).receive(caseArgumentEvidence2, DocumentTag.CASE_ARGUMENT);

        verify(documentsAppender, times(1)).append(filteredLegalRepresentativeDocumentsCaptor.capture(), eq(caseArgumentEvidenceWithMetadata));

        List<IdValue<DocumentWithMetadata>> filteredLegalRepresentativeDocuments =
            filteredLegalRepresentativeDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, filteredLegalRepresentativeDocuments.size());

        verify(documentsAppender, times(1)).append(allLegalRepresentativeDocuments, Collections.singletonList(caseArgumentWithMetadata));

        verify(asylumCase, times(1)).setLegalRepresentativeDocuments(allLegalRepresentativeDocuments);
    }

    @Test
    public void should_replace_any_existing_case_argument_documents_for_the_case() {

        DocumentWithMetadata existingCaseArgumentDocument1 = mock(DocumentWithMetadata.class);
        DocumentWithMetadata existingCaseArgumentDocument2 = mock(DocumentWithMetadata.class);
        DocumentWithMetadata existingCaseArgumentDocument3 = mock(DocumentWithMetadata.class);

        List<IdValue<DocumentWithMetadata>> existingLegalRepresentativeDocuments =
            Arrays.asList(
                new IdValue<>("1", existingCaseArgumentDocument1),
                new IdValue<>("2", existingCaseArgumentDocument2),
                new IdValue<>("3", existingCaseArgumentDocument3)
            );

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

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getLegalRepresentativeDocuments()).thenReturn(Optional.of(existingLegalRepresentativeDocuments));
        when(asylumCase.getCaseArgumentDocument()).thenReturn(Optional.of(caseArgumentDocument));
        when(asylumCase.getCaseArgumentDescription()).thenReturn(Optional.of(caseArgumentDescription));
        when(asylumCase.getCaseArgumentEvidence()).thenReturn(Optional.of(caseArgumentEvidence));

        when(existingCaseArgumentDocument1.getTag()).thenReturn(DocumentTag.CASE_ARGUMENT);
        when(existingCaseArgumentDocument2.getTag()).thenReturn(DocumentTag.RESPONDENT_EVIDENCE);
        when(existingCaseArgumentDocument3.getTag()).thenReturn(DocumentTag.CASE_ARGUMENT);

        when(documentReceiver.receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT))
            .thenReturn(Optional.of(caseArgumentWithMetadata));

        when(documentReceiver.receive(caseArgumentEvidence1, DocumentTag.CASE_ARGUMENT))
            .thenReturn(Optional.of(caseArgumentEvidence1WithMetadata));

        when(documentReceiver.receive(caseArgumentEvidence2, DocumentTag.CASE_ARGUMENT))
            .thenReturn(Optional.of(caseArgumentEvidence2WithMetadata));

        when(documentsAppender.append(any(List.class), eq(caseArgumentEvidenceWithMetadata)))
            .thenReturn(allLegalRepresentativeDocuments);

        when(documentsAppender.append(allLegalRepresentativeDocuments, Collections.singletonList(caseArgumentWithMetadata)))
            .thenReturn(allLegalRepresentativeDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            buildCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).getCaseArgumentDocument();
        verify(asylumCase, times(1)).getCaseArgumentDescription();
        verify(asylumCase, times(1)).getCaseArgumentEvidence();

        verify(documentReceiver, times(1)).receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT);
        verify(documentReceiver, times(1)).receive(caseArgumentEvidence1, DocumentTag.CASE_ARGUMENT);
        verify(documentReceiver, times(1)).receive(caseArgumentEvidence2, DocumentTag.CASE_ARGUMENT);

        verify(documentsAppender, times(1)).append(filteredLegalRepresentativeDocumentsCaptor.capture(), eq(caseArgumentEvidenceWithMetadata));

        List<IdValue<DocumentWithMetadata>> filteredLegalRepresentativeDocuments =
            filteredLegalRepresentativeDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals("Existing case argument is filtered out to be replaced", 1, filteredLegalRepresentativeDocuments.size());

        verify(documentsAppender, times(1)).append(allLegalRepresentativeDocuments, Collections.singletonList(caseArgumentWithMetadata));

        verify(asylumCase, times(1)).setLegalRepresentativeDocuments(allLegalRepresentativeDocuments);
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

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getLegalRepresentativeDocuments()).thenReturn(Optional.empty());
        when(asylumCase.getCaseArgumentDocument()).thenReturn(Optional.of(caseArgumentDocument));
        when(asylumCase.getCaseArgumentDescription()).thenReturn(Optional.of(caseArgumentDescription));
        when(asylumCase.getCaseArgumentEvidence()).thenReturn(Optional.of(caseArgumentEvidence));

        when(documentReceiver.receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT))
            .thenReturn(Optional.of(caseArgumentWithMetadata));

        when(documentReceiver.receive(caseArgumentEvidence1, DocumentTag.CASE_ARGUMENT))
            .thenReturn(Optional.of(caseArgumentEvidence1WithMetadata));

        when(documentReceiver.receive(caseArgumentEvidence2, DocumentTag.CASE_ARGUMENT))
            .thenReturn(Optional.of(caseArgumentEvidence2WithMetadata));

        when(documentsAppender.append(any(List.class), eq(caseArgumentEvidenceWithMetadata)))
            .thenReturn(allLegalRepresentativeDocuments);

        when(documentsAppender.append(allLegalRepresentativeDocuments, Collections.singletonList(caseArgumentWithMetadata)))
            .thenReturn(allLegalRepresentativeDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            buildCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).getCaseArgumentDocument();
        verify(asylumCase, times(1)).getCaseArgumentDescription();
        verify(asylumCase, times(1)).getCaseArgumentEvidence();

        verify(documentReceiver, times(1)).receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT);
        verify(documentReceiver, times(1)).receive(caseArgumentEvidence1, DocumentTag.CASE_ARGUMENT);
        verify(documentReceiver, times(1)).receive(caseArgumentEvidence2, DocumentTag.CASE_ARGUMENT);

        verify(documentsAppender, times(1)).append(filteredLegalRepresentativeDocumentsCaptor.capture(), eq(caseArgumentEvidenceWithMetadata));

        List<IdValue<DocumentWithMetadata>> filteredLegalRepresentativeDocuments =
            filteredLegalRepresentativeDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, filteredLegalRepresentativeDocuments.size());

        verify(documentsAppender, times(1)).append(allLegalRepresentativeDocuments, Collections.singletonList(caseArgumentWithMetadata));

        verify(asylumCase, times(1)).setLegalRepresentativeDocuments(allLegalRepresentativeDocuments);
    }

    @Test
    public void should_throw_when_case_argument_document_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.getCaseArgumentDocument()).thenReturn(Optional.empty());

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
