package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

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
public class CreateCaseSummaryHandlerTest {

    @Mock private DocumentReceiver documentReceiver;
    @Mock private DocumentsAppender documentsAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document caseSummaryDocument;
    private String caseSummaryDescription = "Case summary description";
    @Mock private DocumentWithMetadata caseSummaryWithMetadata;
    @Mock private List<IdValue<DocumentWithMetadata>> existingHearingDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> allHearingDocuments;

    @Captor private ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> hearingDocumentsCaptor;

    private CreateCaseSummaryHandler createCaseSummaryHandler;

    @Before
    public void setUp() {
        createCaseSummaryHandler =
            new CreateCaseSummaryHandler(
                documentReceiver,
                documentsAppender
            );

        when(callback.getEvent()).thenReturn(Event.CREATE_CASE_SUMMARY);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(documentReceiver.receive(
            caseSummaryDocument,
            caseSummaryDescription,
            DocumentTag.CASE_SUMMARY
        )).thenReturn(caseSummaryWithMetadata);

        when(documentsAppender.append(
            any(List.class),
            eq(Collections.singletonList(caseSummaryWithMetadata)),
            eq(DocumentTag.CASE_SUMMARY)
        )).thenReturn(allHearingDocuments);
    }

    @Test
    public void should_append_case_summary_to_hearing_documents_for_the_case() {

        when(asylumCase.read(HEARING_DOCUMENTS)).thenReturn(Optional.of(existingHearingDocuments));
        when(asylumCase.read(CASE_SUMMARY_DOCUMENT, Document.class)).thenReturn(Optional.of(caseSummaryDocument));
        when(asylumCase.read(CASE_SUMMARY_DESCRIPTION,String.class)).thenReturn(Optional.of(caseSummaryDescription));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            createCaseSummaryHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(CASE_SUMMARY_DOCUMENT, Document.class);
        verify(asylumCase, times(1)).read(CASE_SUMMARY_DESCRIPTION, String.class);

        verify(documentReceiver, times(1)).receive(caseSummaryDocument, caseSummaryDescription, DocumentTag.CASE_SUMMARY);

        verify(documentsAppender, times(1))
            .append(
                existingHearingDocuments,
                Collections.singletonList(caseSummaryWithMetadata),
                DocumentTag.CASE_SUMMARY
            );

        verify(asylumCase, times(1)).write(HEARING_DOCUMENTS, allHearingDocuments);
    }

    @Test
    public void should_append_case_summary_to_reheard_hearing_documents_for_the_case() {

        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS)).thenReturn(Optional.of(existingHearingDocuments));
        when(asylumCase.read(CASE_SUMMARY_DOCUMENT, Document.class)).thenReturn(Optional.of(caseSummaryDocument));
        when(asylumCase.read(CASE_SUMMARY_DESCRIPTION,String.class)).thenReturn(Optional.of(caseSummaryDescription));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            createCaseSummaryHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(CASE_SUMMARY_DOCUMENT, Document.class);
        verify(asylumCase, times(1)).read(CASE_SUMMARY_DESCRIPTION, String.class);

        verify(documentReceiver, times(1)).receive(caseSummaryDocument, caseSummaryDescription, DocumentTag.CASE_SUMMARY);

        verify(documentsAppender, times(1))
            .append(
                existingHearingDocuments,
                Collections.singletonList(caseSummaryWithMetadata),
                DocumentTag.CASE_SUMMARY
            );

        verify(asylumCase, times(0)).write(HEARING_DOCUMENTS, allHearingDocuments);
        verify(asylumCase, times(1)).write(REHEARD_HEARING_DOCUMENTS, allHearingDocuments);
    }

    @Test
    public void should_add_case_summary_to_the_case_when_no_hearing_documents_exist() {

        when(asylumCase.read(HEARING_DOCUMENTS)).thenReturn(Optional.empty());
        when(asylumCase.read(CASE_SUMMARY_DOCUMENT, Document.class)).thenReturn(Optional.of(caseSummaryDocument));
        when(asylumCase.read(CASE_SUMMARY_DESCRIPTION, String.class)).thenReturn(Optional.of(caseSummaryDescription));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            createCaseSummaryHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(CASE_SUMMARY_DOCUMENT, Document.class);
        verify(asylumCase, times(1)).read(CASE_SUMMARY_DESCRIPTION, String.class);

        verify(documentReceiver, times(1)).receive(caseSummaryDocument, caseSummaryDescription, DocumentTag.CASE_SUMMARY);

        verify(documentsAppender, times(1))
            .append(
                hearingDocumentsCaptor.capture(),
                eq(Collections.singletonList(caseSummaryWithMetadata)),
                eq(DocumentTag.CASE_SUMMARY)
            );

        List<IdValue<DocumentWithMetadata>> hearingDocuments =
            hearingDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, hearingDocuments.size());

        verify(asylumCase, times(1)).write(HEARING_DOCUMENTS, allHearingDocuments);
    }

    @Test
    public void should_add_case_summary_to_the_case_when_no_reheard_hearing_documents_exist() {

        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS)).thenReturn(Optional.empty());
        when(asylumCase.read(CASE_SUMMARY_DOCUMENT, Document.class)).thenReturn(Optional.of(caseSummaryDocument));
        when(asylumCase.read(CASE_SUMMARY_DESCRIPTION, String.class)).thenReturn(Optional.of(caseSummaryDescription));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            createCaseSummaryHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(CASE_SUMMARY_DOCUMENT, Document.class);
        verify(asylumCase, times(1)).read(CASE_SUMMARY_DESCRIPTION, String.class);

        verify(documentReceiver, times(1)).receive(caseSummaryDocument, caseSummaryDescription, DocumentTag.CASE_SUMMARY);

        verify(documentsAppender, times(1))
            .append(
                hearingDocumentsCaptor.capture(),
                eq(Collections.singletonList(caseSummaryWithMetadata)),
                eq(DocumentTag.CASE_SUMMARY)
            );

        List<IdValue<DocumentWithMetadata>> hearingDocuments =
            hearingDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, hearingDocuments.size());

        verify(asylumCase, times(0)).write(HEARING_DOCUMENTS, allHearingDocuments);
        verify(asylumCase, times(1)).write(REHEARD_HEARING_DOCUMENTS, allHearingDocuments);

    }

    @Test
    public void should_throw_when_case_summary_document_is_not_present() {

        when(asylumCase.read(CASE_SUMMARY_DOCUMENT, Document.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createCaseSummaryHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("caseSummaryDocument is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> createCaseSummaryHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> createCaseSummaryHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = createCaseSummaryHandler.canHandle(callbackStage, callback);

                if (event == Event.CREATE_CASE_SUMMARY
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

        assertThatThrownBy(() -> createCaseSummaryHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> createCaseSummaryHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> createCaseSummaryHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> createCaseSummaryHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
