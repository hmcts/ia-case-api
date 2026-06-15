package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class UploadSignedDecisionNoticeHandlerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private DocumentWithMetadata unsignedDecisionNoticeMetadata1;
    @Mock
    private DocumentWithMetadata tribunalDocument1;
    @Mock
    private DocumentWithMetadata tribunalDocument2;

    private UploadSignedDecisionNoticeHandler uploadSignedDecisionNoticeHandler;

    private final LocalDateTime nowWithTime = LocalDateTime.now();

    @BeforeEach
    public void setUp() {
        uploadSignedDecisionNoticeHandler = new UploadSignedDecisionNoticeHandler(dateProvider);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_SIGNED_DECISION_NOTICE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(dateProvider.nowWithTime()).thenReturn(nowWithTime);
        when(unsignedDecisionNoticeMetadata1.getTag()).thenReturn(DocumentTag.BAIL_DECISION_UNSIGNED);
        when(tribunalDocument1.getTag()).thenReturn(DocumentTag.UPLOAD_DOCUMENT);
        when(tribunalDocument2.getTag()).thenReturn(DocumentTag.BAIL_SUBMISSION);
    }

    @Test
    void should_add_outcome_date_state_and_remove_unsigned_doc_from_tribunal() {
        List<IdValue<DocumentWithMetadata>> tribunalDocumentsWithUnsignedDoc = List.of(
                new IdValue<>("1", tribunalDocument1),
                new IdValue<>("2", tribunalDocument2),
                new IdValue<>("3", unsignedDecisionNoticeMetadata1));
        when(bailCase.read(TRIBUNAL_DOCUMENTS_WITH_METADATA)).thenReturn(Optional.of(tribunalDocumentsWithUnsignedDoc));
        final List<IdValue<DocumentWithMetadata>> tribunalDocumentsWithoutUnSignedDoc = List.of(
                new IdValue<>("1", tribunalDocument1),
                new IdValue<>("2", tribunalDocument2));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
                uploadSignedDecisionNoticeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());
        verify(bailCase, times(1)).read(TRIBUNAL_DOCUMENTS_WITH_METADATA);
        //Verify - No Document is left in the Tribunal Documents
        verify(bailCase, times(1))
                .write(TRIBUNAL_DOCUMENTS_WITH_METADATA, tribunalDocumentsWithoutUnSignedDoc);
        verify(bailCase).write(OUTCOME_DATE, nowWithTime.toString());
        verify(bailCase, times(1)).write(OUTCOME_STATE, State.DECISION_DECIDED);
        verify(bailCase, times(1)).write(HAS_BEEN_RELISTED, YesOrNo.NO);
        verify(bailCase, times(1)).clear(DECISION_UNSIGNED_DOCUMENT);
    }

    @Test
    void should_handle_when_tribunal_collection_not_contains_unsigned_document() {
        List<IdValue<DocumentWithMetadata>> tribunalDocuments = List.of(
                new IdValue<>("1", tribunalDocument1),
                new IdValue<>("2", tribunalDocument2));
        when(bailCase.read(TRIBUNAL_DOCUMENTS_WITH_METADATA)).thenReturn(Optional.of(tribunalDocuments));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
                uploadSignedDecisionNoticeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());
        verify(bailCase, times(1)).read(TRIBUNAL_DOCUMENTS_WITH_METADATA);
        //Verify - Tribunal Documents are same, as there is no Unsigned Document
        verify(bailCase, times(1))
                .write(TRIBUNAL_DOCUMENTS_WITH_METADATA, tribunalDocuments);
        verify(bailCase).write(OUTCOME_DATE, nowWithTime.toString());
        verify(bailCase, times(1)).write(OUTCOME_STATE, State.DECISION_DECIDED);
        verify(bailCase, times(1)).write(HAS_BEEN_RELISTED, YesOrNo.NO);
    }

    @Test
    void should_handle_when_tribunal_collection_is_empty() {
        List<IdValue<DocumentWithMetadata>> tribunalDocuments = Collections.EMPTY_LIST;
        when(bailCase.read(TRIBUNAL_DOCUMENTS_WITH_METADATA)).thenReturn(Optional.of(tribunalDocuments));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
                uploadSignedDecisionNoticeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());
        verify(bailCase, times(1)).read(TRIBUNAL_DOCUMENTS_WITH_METADATA);
        //Verify - Tribunal Documents are same, as there is no document
        verify(bailCase, times(1))
                .write(TRIBUNAL_DOCUMENTS_WITH_METADATA, tribunalDocuments);
        verify(bailCase).write(OUTCOME_DATE, nowWithTime.toString());
        verify(bailCase, times(1)).write(OUTCOME_STATE, State.DECISION_DECIDED);
        verify(bailCase, times(1)).write(HAS_BEEN_RELISTED, YesOrNo.NO);
    }

    @Test
    void should_get_dispatch_priority_as_latest() {
        DispatchPriority dispatchPriority =
                uploadSignedDecisionNoticeHandler.getDispatchPriority();

        assertNotNull(dispatchPriority);
        assertEquals(DispatchPriority.LATEST, dispatchPriority);
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
