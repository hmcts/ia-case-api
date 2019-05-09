package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AddAppealResponseHandlerTest {

    @Mock private DocumentReceiver documentReceiver;
    @Mock private DocumentsAppender documentsAppender;
    @Mock private Callback<CaseDataMap> callback;
    @Mock private CaseDetails<CaseDataMap> caseDetails;
    @Mock private CaseDataMap CaseDataMap;
    @Mock private Document appealResponseDocument;
    private String appealResponseDescription = "Appeal response description";
    @Mock private DocumentWithMetadata appealResponseWithMetadata;
    @Mock private DocumentWithDescription appealResponseEvidence1;
    @Mock private DocumentWithDescription appealResponseEvidence2;
    @Mock private DocumentWithMetadata appealResponseEvidence1WithMetadata;
    @Mock private DocumentWithMetadata appealResponseEvidence2WithMetadata;
    @Mock private List<IdValue<DocumentWithMetadata>> existingRespondentDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> allRespondentDocuments;

    @Captor private ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> respondentDocumentsCaptor;

    private AddAppealResponseHandler addAppealResponseHandler;

    @Before
    public void setUp() {
        addAppealResponseHandler =
            new AddAppealResponseHandler(
                documentReceiver,
                documentsAppender
            );
    }

    @Test
    public void should_be_handled_early() {
        assertEquals(DispatchPriority.EARLY, addAppealResponseHandler.getDispatchPriority());
    }

    @Test
    public void should_append_appeal_response_to_respondent_documents_for_the_case() {

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
        when(caseDetails.getCaseData()).thenReturn(CaseDataMap);
        when(CaseDataMap.getRespondentDocuments()).thenReturn(Optional.of(existingRespondentDocuments));
        when(CaseDataMap.getAppealResponseDocument()).thenReturn(Optional.of(appealResponseDocument));
        when(CaseDataMap.getAppealResponseDescription()).thenReturn(Optional.of(appealResponseDescription));
        when(CaseDataMap.getAppealResponseEvidence()).thenReturn(Optional.of(appealResponseEvidence));

        when(documentReceiver.receive(appealResponseDocument, appealResponseDescription, DocumentTag.APPEAL_RESPONSE))
            .thenReturn(appealResponseWithMetadata);

        when(documentReceiver.tryReceiveAll(appealResponseEvidence, DocumentTag.APPEAL_RESPONSE))
            .thenReturn(appealResponseEvidenceWithMetadata);

        when(documentsAppender.append(existingRespondentDocuments, appealResponseDocumentsWithMetadata, DocumentTag.APPEAL_RESPONSE))
            .thenReturn(allRespondentDocuments);

        PreSubmitCallbackResponse<CaseDataMap> callbackResponse =
            addAppealResponseHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(CaseDataMap, callbackResponse.getData());

        verify(CaseDataMap, times(1)).getAppealResponseDocument();
        verify(CaseDataMap, times(1)).getAppealResponseDescription();
        verify(CaseDataMap, times(1)).getAppealResponseEvidence();

        verify(documentReceiver, times(1)).receive(appealResponseDocument, appealResponseDescription, DocumentTag.APPEAL_RESPONSE);
        verify(documentReceiver, times(1)).tryReceiveAll(appealResponseEvidence, DocumentTag.APPEAL_RESPONSE);

        verify(documentsAppender, times(1))
            .append(
                existingRespondentDocuments,
                appealResponseDocumentsWithMetadata,
                DocumentTag.APPEAL_RESPONSE
            );

        verify(CaseDataMap, times(1)).setRespondentDocuments(allRespondentDocuments);
        verify(CaseDataMap, times(1)).setAppealResponseAvailable(YesOrNo.Yes);
    }

    @Test
    public void should_add_appeal_response_to_the_case_when_no_respondent_documents_exist() {

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
        when(caseDetails.getCaseData()).thenReturn(CaseDataMap);
        when(CaseDataMap.getRespondentDocuments()).thenReturn(Optional.empty());
        when(CaseDataMap.getAppealResponseDocument()).thenReturn(Optional.of(appealResponseDocument));
        when(CaseDataMap.getAppealResponseDescription()).thenReturn(Optional.of(appealResponseDescription));
        when(CaseDataMap.getAppealResponseEvidence()).thenReturn(Optional.of(appealResponseEvidence));

        when(documentReceiver.receive(appealResponseDocument, appealResponseDescription, DocumentTag.APPEAL_RESPONSE))
            .thenReturn(appealResponseWithMetadata);

        when(documentReceiver.tryReceiveAll(appealResponseEvidence, DocumentTag.APPEAL_RESPONSE))
            .thenReturn(appealResponseEvidenceWithMetadata);

        when(documentsAppender.append(any(List.class), eq(appealResponseDocumentsWithMetadata), eq(DocumentTag.APPEAL_RESPONSE)))
            .thenReturn(allRespondentDocuments);

        PreSubmitCallbackResponse<CaseDataMap> callbackResponse =
            addAppealResponseHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(CaseDataMap, callbackResponse.getData());

        verify(CaseDataMap, times(1)).getAppealResponseDocument();
        verify(CaseDataMap, times(1)).getAppealResponseDescription();
        verify(CaseDataMap, times(1)).getAppealResponseEvidence();

        verify(documentReceiver, times(1)).receive(appealResponseDocument, appealResponseDescription, DocumentTag.APPEAL_RESPONSE);
        verify(documentReceiver, times(1)).tryReceiveAll(appealResponseEvidence, DocumentTag.APPEAL_RESPONSE);

        verify(documentsAppender, times(1))
            .append(
                respondentDocumentsCaptor.capture(),
                eq(appealResponseDocumentsWithMetadata),
                eq(DocumentTag.APPEAL_RESPONSE)
            );

        List<IdValue<DocumentWithMetadata>> respondentDocuments =
            respondentDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, respondentDocuments.size());

        verify(CaseDataMap, times(1)).setRespondentDocuments(allRespondentDocuments);
        verify(CaseDataMap, times(1)).setAppealResponseAvailable(YesOrNo.Yes);
    }

    @Test
    public void should_throw_when_appeal_response_document_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.ADD_APPEAL_RESPONSE);
        when(caseDetails.getCaseData()).thenReturn(CaseDataMap);

        when(CaseDataMap.getAppealResponseDocument()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addAppealResponseHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("appealResponseDocument is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> addAppealResponseHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> addAppealResponseHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : values()) {

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
    public void should_not_allow_null_arguments() {

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
