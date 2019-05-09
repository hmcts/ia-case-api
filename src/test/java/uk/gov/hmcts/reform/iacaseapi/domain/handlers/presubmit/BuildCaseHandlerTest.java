package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
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
    @Mock private Callback<CaseDataMap> callback;
    @Mock private CaseDetails<CaseDataMap> caseDetails;
    @Mock private CaseDataMap CaseDataMap;
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
        when(caseDetails.getCaseData()).thenReturn(CaseDataMap);
        when(CaseDataMap.getLegalRepresentativeDocuments()).thenReturn(Optional.of(existingLegalRepresentativeDocuments));
        when(CaseDataMap.getCaseArgumentDocument()).thenReturn(Optional.of(caseArgumentDocument));
        when(CaseDataMap.getCaseArgumentDescription()).thenReturn(Optional.of(caseArgumentDescription));
        when(CaseDataMap.getCaseArgumentEvidence()).thenReturn(Optional.of(caseArgumentEvidence));

        when(documentReceiver.receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT))
            .thenReturn(caseArgumentWithMetadata);

        when(documentReceiver.tryReceiveAll(caseArgumentEvidence, DocumentTag.CASE_ARGUMENT))
            .thenReturn(caseArgumentEvidenceWithMetadata);

        when(documentsAppender.append(existingLegalRepresentativeDocuments, caseArgumentDocumentsWithMetadata, DocumentTag.CASE_ARGUMENT))
            .thenReturn(allLegalRepresentativeDocuments);

        PreSubmitCallbackResponse<CaseDataMap> callbackResponse =
            buildCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(CaseDataMap, callbackResponse.getData());

        verify(CaseDataMap, times(1)).getCaseArgumentDocument();
        verify(CaseDataMap, times(1)).getCaseArgumentDescription();
        verify(CaseDataMap, times(1)).getCaseArgumentEvidence();

        verify(documentReceiver, times(1)).receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT);
        verify(documentReceiver, times(1)).tryReceiveAll(caseArgumentEvidence, DocumentTag.CASE_ARGUMENT);

        verify(documentsAppender, times(1))
            .append(
                existingLegalRepresentativeDocuments,
                caseArgumentDocumentsWithMetadata,
                DocumentTag.CASE_ARGUMENT
            );

        verify(CaseDataMap, times(1)).setLegalRepresentativeDocuments(allLegalRepresentativeDocuments);
        verify(CaseDataMap, times(1)).setCaseArgumentAvailable(YesOrNo.YES);
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
        when(caseDetails.getCaseData()).thenReturn(CaseDataMap);
        when(CaseDataMap.getLegalRepresentativeDocuments()).thenReturn(Optional.empty());
        when(CaseDataMap.getCaseArgumentDocument()).thenReturn(Optional.of(caseArgumentDocument));
        when(CaseDataMap.getCaseArgumentDescription()).thenReturn(Optional.of(caseArgumentDescription));
        when(CaseDataMap.getCaseArgumentEvidence()).thenReturn(Optional.of(caseArgumentEvidence));

        when(documentReceiver.receive(caseArgumentDocument, caseArgumentDescription, DocumentTag.CASE_ARGUMENT))
            .thenReturn(caseArgumentWithMetadata);

        when(documentReceiver.tryReceiveAll(caseArgumentEvidence, DocumentTag.CASE_ARGUMENT))
            .thenReturn(caseArgumentEvidenceWithMetadata);

        when(documentsAppender.append(any(List.class), eq(caseArgumentDocumentsWithMetadata), eq(DocumentTag.CASE_ARGUMENT)))
            .thenReturn(allLegalRepresentativeDocuments);

        PreSubmitCallbackResponse<CaseDataMap> callbackResponse =
            buildCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(CaseDataMap, callbackResponse.getData());

        verify(CaseDataMap, times(1)).getCaseArgumentDocument();
        verify(CaseDataMap, times(1)).getCaseArgumentDescription();
        verify(CaseDataMap, times(1)).getCaseArgumentEvidence();

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

        verify(CaseDataMap, times(1)).setLegalRepresentativeDocuments(allLegalRepresentativeDocuments);
        verify(CaseDataMap, times(1)).setCaseArgumentAvailable(YesOrNo.YES);
    }

    @Test
    public void should_throw_when_case_argument_document_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(caseDetails.getCaseData()).thenReturn(CaseDataMap);

        when(CaseDataMap.getCaseArgumentDocument()).thenReturn(Optional.empty());

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
