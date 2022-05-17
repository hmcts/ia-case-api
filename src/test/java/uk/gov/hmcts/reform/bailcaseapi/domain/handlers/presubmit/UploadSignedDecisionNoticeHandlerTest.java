package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT;

import java.util.ArrayList;
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
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentsAppender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class UploadSignedDecisionNoticeHandlerTest {

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
    private Document signedDecisionNotice1;
    @Mock
    private DocumentWithMetadata signedDecisionNoticeMetadata1;
    @Mock
    private List<IdValue<DocumentWithMetadata>> newSignedDecisionNotice;

    private List<IdValue<DocumentWithMetadata>> existingTribunalDocumentsWithMetadata = new ArrayList<>();

    @Captor
    private ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> existingDecisionNoticeDocumentsCaptor;

    private UploadSignedDecisionNoticeHandler uploadSignedDecisionNoticeHandler;

    @BeforeEach
    public void setUp() {
        uploadSignedDecisionNoticeHandler =
            new UploadSignedDecisionNoticeHandler(
                documentReceiver,
                documentsAppender
            );
        when(callback.getEvent()).thenReturn(Event.UPLOAD_SIGNED_DECISION_NOTICE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
    }

    @Test
    void should_add_new_document_to_the_case() {

        List<IdValue<DocumentWithMetadata>> allTribunalDocs =
            List.of(
                new IdValue<>("1", signedDecisionNoticeMetadata1)
            );

        when(bailCase.read(UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT, Document.class)).thenReturn(
            Optional.of(signedDecisionNotice1));

        when(bailCase.read(TRIBUNAL_DOCUMENTS_WITH_METADATA)).thenReturn(
            Optional.empty());

        when(documentReceiver.tryReceive(new DocumentWithDescription(signedDecisionNotice1, ""),
                                         DocumentTag.SIGNED_DECISION_NOTICE))
            .thenReturn(Optional.of(signedDecisionNoticeMetadata1));

        when(documentsAppender.append(eq(new ArrayList<>()), eq(List.of(signedDecisionNoticeMetadata1))))
            .thenReturn(allTribunalDocs);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            uploadSignedDecisionNoticeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).read(UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT, Document.class);

        verify(documentReceiver, times(1)).tryReceive(
            new DocumentWithDescription(signedDecisionNotice1, ""),
            DocumentTag.SIGNED_DECISION_NOTICE);

        verify(documentsAppender, times(1))
            .append(existingDecisionNoticeDocumentsCaptor.capture(), eq(List.of(signedDecisionNoticeMetadata1)));

        List<IdValue<DocumentWithMetadata>> actualExistingDecisionNoticeDocuments =
            existingDecisionNoticeDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, actualExistingDecisionNoticeDocuments.size());

        verify(bailCase, times(1)).write(TRIBUNAL_DOCUMENTS_WITH_METADATA, allTribunalDocs);

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> uploadSignedDecisionNoticeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> uploadSignedDecisionNoticeHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> uploadSignedDecisionNoticeHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadSignedDecisionNoticeHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadSignedDecisionNoticeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
