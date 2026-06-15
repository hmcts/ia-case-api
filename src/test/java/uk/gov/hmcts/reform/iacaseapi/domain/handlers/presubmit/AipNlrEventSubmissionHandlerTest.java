package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.EditDocsAuditLogService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipNlrEventSubmissionHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Appender<CaseNote> appender;
    @Mock
    private EditDocsAuditLogService editDocsAuditLogService;
    @Mock
    private List<IdValue<CaseNote>> mockList;

    @Captor
    private ArgumentCaptor<CaseNote> caseNoteCaptor;

    @InjectMocks
    private AipNlrEventSubmissionHandler handler;

    private final String forenameAndSurname = "John Doe";
    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        String forename = "John";
        String surname = "Doe";
        when(asylumCase.read(AsylumCaseFieldDefinition.NLR_DETAILS, NonLegalRepDetails.class))
            .thenReturn(Optional.of(NonLegalRepDetails.builder().givenNames(forename).familyName(surname).build()));
    }

    @Test
    void getDispatchPriority_should_return_last() {
        assertEquals(DispatchPriority.LAST, handler.getDispatchPriority());
    }

    @Test
    void canHandle_should_throw_exception_for_null_arguments() {
        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> handler.canHandle(null, callback));
        assertEquals("callbackStage must not be null", exception.getMessage());

        exception =
            assertThrows(NullPointerException.class, () -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_SUBMIT"}, mode = EnumSource.Mode.EXCLUDE)
    void canHandle_should_return_false_for_invalid_stage(PreSubmitCallbackStage callbackStage) {
        assertFalse(handler.canHandle(callbackStage, callback));
    }

    @Test
    void canHandle_should_return_false_for_hasNlrSubmitted_no() {
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(Event.class)
    void canHandle_should_return_true_for_hasNlrSubmitted_yes(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(AsylumCaseFieldDefinition.HAS_NLR_SUBMITTED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void handle_should_throw_exception_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(AsylumCaseFieldDefinition.HAS_NLR_SUBMITTED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
        assertEquals("Cannot handle callback", exception.getMessage());
    }

    @Test
    void handle_should_throw_if_no_nlr_name() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(AsylumCaseFieldDefinition.HAS_NLR_SUBMITTED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseFieldDefinition.NLR_DETAILS, NonLegalRepDetails.class))
            .thenReturn(Optional.empty());
        when(editDocsAuditLogService.getUploadedOrGeneratedDocumentNames(callback)).thenReturn(emptyList());
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
        assertEquals("Non-legal representative details are not present", exception.getMessage());
    }

    @Test
    void handle_should_clear_hasNlrSubmitted() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(AsylumCaseFieldDefinition.HAS_NLR_SUBMITTED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(editDocsAuditLogService.getUploadedOrGeneratedDocumentNames(callback)).thenReturn(emptyList());
        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.HAS_NLR_SUBMITTED);
    }

    @ParameterizedTest
    @EnumSource(Event.class)
    void handle_should_create_case_notes_if_empty_doc_list_and_none_existing(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(AsylumCaseFieldDefinition.HAS_NLR_SUBMITTED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(editDocsAuditLogService.getUploadedOrGeneratedDocumentNames(callback)).thenReturn(emptyList());
        when(appender.append(any(CaseNote.class), eq(emptyList()))).thenReturn(mockList);
        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.HAS_NLR_SUBMITTED);
        verify(appender, times(1)).append(caseNoteCaptor.capture(), eq(emptyList()));
        CaseNote capturedCaseNote = caseNoteCaptor.getValue();
        assertEquals("NLR event submission - " + event.toString(), capturedCaseNote.getCaseNoteSubject());
        assertEquals("Non-legal representative submitted the event on behalf of the appellant.", capturedCaseNote.getCaseNoteDescription());
        assertEquals(forenameAndSurname, capturedCaseNote.getUser());
        verify(asylumCase, times(1)).write(AsylumCaseFieldDefinition.CASE_NOTES, mockList);
    }

    @Test
    void handle_should_append_if_non_empty_doc_list_and_existing() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(AsylumCaseFieldDefinition.HAS_NLR_SUBMITTED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(editDocsAuditLogService.getUploadedOrGeneratedDocumentNames(callback)).thenReturn(List.of("doc1.pdf", "doc2.docx"));
        CaseNote caseNote = mock(CaseNote.class);
        IdValue<CaseNote> idValue = new IdValue<>("123", caseNote);
        when(asylumCase.read(AsylumCaseFieldDefinition.CASE_NOTES)).thenReturn(Optional.of(List.of(idValue)));
        when(appender.append(any(CaseNote.class), eq(List.of(idValue)))).thenReturn(mockList);
        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase, times(1)).clear(AsylumCaseFieldDefinition.HAS_NLR_SUBMITTED);
        verify(appender, times(1)).append(caseNoteCaptor.capture(), eq(List.of(idValue)));
        verify(asylumCase, times(1)).write(AsylumCaseFieldDefinition.CASE_NOTES, mockList);
        CaseNote capturedCaseNote = caseNoteCaptor.getValue();
        assertEquals("NLR event submission - submitAppeal" , capturedCaseNote.getCaseNoteSubject());
        assertTrue(capturedCaseNote.getCaseNoteDescription()
            .contains("Non-legal representative submitted the event on behalf of the appellant."));
        assertTrue(capturedCaseNote.getCaseNoteDescription()
            .contains("The following documents were uploaded or generated as a result of this event:"));
        assertTrue(capturedCaseNote.getCaseNoteDescription()
            .contains("doc1.pdf"));
        assertTrue(capturedCaseNote.getCaseNoteDescription()
            .contains("doc2.docx"));
        assertEquals(forenameAndSurname, capturedCaseNote.getUser());
        verify(asylumCase, times(1)).write(AsylumCaseFieldDefinition.CASE_NOTES, mockList);
    }
}