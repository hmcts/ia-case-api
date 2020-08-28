package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AddAppealResponseHandlerTest {

    @Mock private DocumentReceiver documentReceiver;
    @Mock private DocumentsAppender documentsAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document appealResponseDocument;
    String appealResponseDescription = "Appeal response description";
    @Mock private DocumentWithMetadata appealResponseWithMetadata;
    @Mock private DocumentWithDescription appealResponseEvidence1;
    @Mock private DocumentWithDescription appealResponseEvidence2;
    @Mock private DocumentWithMetadata appealResponseEvidence1WithMetadata;
    @Mock private DocumentWithMetadata appealResponseEvidence2WithMetadata;
    @Mock private List<IdValue<DocumentWithMetadata>> existingRespondentDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> allRespondentDocuments;

    @Captor ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> respondentDocumentsCaptor;

    AddAppealResponseHandler addAppealResponseHandler;

    @BeforeEach
    void setUp() {

        addAppealResponseHandler =
            new AddAppealResponseHandler(
                documentReceiver,
                documentsAppender
            );
    }

    @Test
    void should_be_handled_early() {
        assertEquals(DispatchPriority.EARLY, addAppealResponseHandler.getDispatchPriority());
    }

    @Test
    void should_append_appeal_response_to_respondent_documents_for_the_case() {

        List<IdValue<DocumentWithDescription>> appealResponseEvidence =
            Arrays.asList(
                new IdValue<>("1", appealResponseEvidence1),
                new IdValue<>("2", appealResponseEvidence2)
            );

        List<DocumentWithMetadata> appealResponseEvidenceWithMetadata =
            Arrays.asList(
                appealResponseEvidence1WithMetadata,
                appealResponseEvidence2WithMetadata
            );

        List<DocumentWithMetadata> appealResponseDocumentsWithMetadata =
            Arrays.asList(
                appealResponseWithMetadata,
                appealResponseEvidence1WithMetadata,
                appealResponseEvidence2WithMetadata
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.ADD_APPEAL_RESPONSE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(RESPONDENT_DOCUMENTS)).thenReturn(Optional.of(existingRespondentDocuments));
        when(asylumCase.read(APPEAL_RESPONSE_DOCUMENT)).thenReturn(Optional.of(appealResponseDocument));
        when(asylumCase.read(APPEAL_RESPONSE_DESCRIPTION, String.class)).thenReturn(Optional.of(appealResponseDescription));
        when(asylumCase.read(APPEAL_RESPONSE_EVIDENCE)).thenReturn(Optional.of(appealResponseEvidence));

        when(documentReceiver.receive(appealResponseDocument, appealResponseDescription, DocumentTag.APPEAL_RESPONSE))
            .thenReturn(appealResponseWithMetadata);

        when(documentReceiver.tryReceiveAll(appealResponseEvidence, DocumentTag.APPEAL_RESPONSE))
            .thenReturn(appealResponseEvidenceWithMetadata);

        when(documentsAppender.append(existingRespondentDocuments, appealResponseDocumentsWithMetadata))
            .thenReturn(allRespondentDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            addAppealResponseHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(APPEAL_RESPONSE_DOCUMENT);
        verify(asylumCase, times(1)).read(APPEAL_RESPONSE_DESCRIPTION, String.class);
        verify(asylumCase, times(1)).read(APPEAL_RESPONSE_EVIDENCE);

        verify(documentReceiver, times(1)).receive(appealResponseDocument, appealResponseDescription, DocumentTag.APPEAL_RESPONSE);
        verify(documentReceiver, times(1)).tryReceiveAll(appealResponseEvidence, DocumentTag.APPEAL_RESPONSE);

        verify(documentsAppender, times(1))
            .append(
                existingRespondentDocuments,
                appealResponseDocumentsWithMetadata
            );

        verify(asylumCase, times(1)).write(RESPONDENT_DOCUMENTS, allRespondentDocuments);
        verify(asylumCase, times(1)).write(APPEAL_RESPONSE_AVAILABLE, YesOrNo.YES);
    }

    @Test
    void should_add_appeal_response_to_the_case_when_no_respondent_documents_exist() {

        List<IdValue<DocumentWithDescription>> appealResponseEvidence =
            Arrays.asList(
                new IdValue<>("1", appealResponseEvidence1),
                new IdValue<>("2", appealResponseEvidence2)
            );

        List<DocumentWithMetadata> appealResponseEvidenceWithMetadata =
            Arrays.asList(
                appealResponseEvidence1WithMetadata,
                appealResponseEvidence2WithMetadata
            );

        List<DocumentWithMetadata> appealResponseDocumentsWithMetadata =
            Arrays.asList(
                appealResponseWithMetadata,
                appealResponseEvidence1WithMetadata,
                appealResponseEvidence2WithMetadata
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.ADD_APPEAL_RESPONSE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(RESPONDENT_DOCUMENTS)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_RESPONSE_DOCUMENT)).thenReturn(Optional.of(appealResponseDocument));
        when(asylumCase.read(APPEAL_RESPONSE_DESCRIPTION, String.class)).thenReturn(Optional.of(appealResponseDescription));
        when(asylumCase.read(APPEAL_RESPONSE_EVIDENCE)).thenReturn(Optional.of(appealResponseEvidence));

        when(documentReceiver.receive(appealResponseDocument, appealResponseDescription, DocumentTag.APPEAL_RESPONSE))
            .thenReturn(appealResponseWithMetadata);

        when(documentReceiver.tryReceiveAll(appealResponseEvidence, DocumentTag.APPEAL_RESPONSE))
            .thenReturn(appealResponseEvidenceWithMetadata);

        when(documentsAppender.append(any(List.class), eq(appealResponseDocumentsWithMetadata)))
            .thenReturn(allRespondentDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            addAppealResponseHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(APPEAL_RESPONSE_DOCUMENT);
        verify(asylumCase, times(1)).read(APPEAL_RESPONSE_DESCRIPTION, String.class);
        verify(asylumCase, times(1)).read(APPEAL_RESPONSE_EVIDENCE);

        verify(documentReceiver, times(1)).receive(appealResponseDocument, appealResponseDescription, DocumentTag.APPEAL_RESPONSE);
        verify(documentReceiver, times(1)).tryReceiveAll(appealResponseEvidence, DocumentTag.APPEAL_RESPONSE);

        verify(documentsAppender, times(1))
            .append(
                respondentDocumentsCaptor.capture(),
                eq(appealResponseDocumentsWithMetadata)
            );

        List<IdValue<DocumentWithMetadata>> respondentDocuments =
            respondentDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, respondentDocuments.size());

        verify(asylumCase, times(1)).write(RESPONDENT_DOCUMENTS, allRespondentDocuments);
        verify(asylumCase, times(1)).write(APPEAL_RESPONSE_AVAILABLE, YesOrNo.YES);
    }

    @Test
    void should_throw_when_appeal_response_document_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.ADD_APPEAL_RESPONSE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_RESPONSE_DOCUMENT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addAppealResponseHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("appealResponseDocument is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> addAppealResponseHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> addAppealResponseHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = addAppealResponseHandler.canHandle(callbackStage, callback);

                if (event == Event.ADD_APPEAL_RESPONSE
                    && callbackStage == ABOUT_TO_SUBMIT) {

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

        assertThatThrownBy(() -> addAppealResponseHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addAppealResponseHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addAppealResponseHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addAppealResponseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
