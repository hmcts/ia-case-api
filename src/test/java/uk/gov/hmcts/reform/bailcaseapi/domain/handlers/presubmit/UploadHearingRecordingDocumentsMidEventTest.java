package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.HEARING_RECORDING_DOCUMENTS;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingRecordingDocument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UploadHearingRecordingDocumentsMidEventTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    private UploadHearingRecordingDocumentsMidEvent handler;

    @BeforeEach
    public void setUp() {
        handler = new UploadHearingRecordingDocumentsMidEvent();
    }

    @Test
    void should_return_error_when_non_mp3_file_is_uploaded() {

        List<IdValue<HearingRecordingDocument>> hearingRecordingDocuments =
            List.of(
                new IdValue<>("1", new HearingRecordingDocument(
                    new Document("http://example.com/document.pdf", "document.pdf", "document.pdf", "hash"), "Some description"))
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_HEARING_RECORDING);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(HEARING_RECORDING_DOCUMENTS)).thenReturn(Optional.of(hearingRecordingDocuments));

        PreSubmitCallbackResponse<BailCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().contains("All documents must be an mp3 file"));

        verify(bailCase, times(1)).read(HEARING_RECORDING_DOCUMENTS);
    }


    @Test
    void should_allow_upload_when_all_files_are_mp3() {

        List<IdValue<HearingRecordingDocument>> hearingRecordingDocuments =
            List.of(
                new IdValue<>("1", new HearingRecordingDocument(
                    new Document("http://example.com/file.mp3", "document.mp3/binary", "docuument.mp3", "hash"), "Some description"))
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_HEARING_RECORDING);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(HEARING_RECORDING_DOCUMENTS)).thenReturn(Optional.of(hearingRecordingDocuments));

        PreSubmitCallbackResponse<BailCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertEquals(0, response.getErrors().size());

        verify(bailCase, times(1)).read(HEARING_RECORDING_DOCUMENTS);
    }

    @Test
    void should_throw_exception_when_callback_cannot_be_handled() {

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
