package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REMITTED_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_SET_ASIDE_REHEARD_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.COURT_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JUDGES_NAMES_TO_EXCLUDE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REHEARING_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMITTAL_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMITTED_ADDITIONAL_INSTRUCTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SOURCE_OF_REMITTAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_OTHER_REMITTAL_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_REMITTAL_DECISION_DOC;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MARK_APPEAL_AS_REMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemittalDocument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SourceOfRemittal;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class MarkAppealAsRemittedUploadDecisionHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private MarkAppealAsRemittedUploadDecisionHandler markAppealAsRemittedUploadDecisionHandler;
    @Mock
    private Appender<CaseNote> caseNoteAppender;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private List allAppendedCaseNotes;
    @Captor
    private ArgumentCaptor<List<IdValue<CaseNote>>> existingCaseNotesCaptor;
    @Captor
    private ArgumentCaptor<CaseNote> newCaseNoteCaptor;

    private final LocalDate now = LocalDate.now();
    private final String judgeExcluded = "Judge Example";
    private final String courtRefNumber = "CA-000001";
    private final String additionalInstruction = "Additional instruction example";


    private final Document remittalDocument =  new Document("http://localhost/documents/123456",
                    "http://localhost/documents/123456",
                    "remittalDecision.pdf");
    private final Document document1 =
        new Document("http://localhost/documents/123456",
            "http://localhost/documents/123456",
            "other documents.pdf");

    private DocumentWithDescription remittalDoc1 = new DocumentWithDescription(document1, "description1");

    private DocumentWithDescription remittalDoc2 = new DocumentWithDescription(document1, "description2");

    @Mock
    private List<IdValue<RemittalDocument>> remittalDocuments;
    @Mock
    private Appender<RemittalDocument> remittalDocumentsAppender;

    @BeforeEach
    public void setUp() {

        MockitoAnnotations.openMocks(this);

        markAppealAsRemittedUploadDecisionHandler = new MarkAppealAsRemittedUploadDecisionHandler(caseNoteAppender, dateProvider, remittalDocumentsAppender);
        when(callback.getEvent()).thenReturn(MARK_APPEAL_AS_REMITTED);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_rename_remittal_decision_document_and_set_fields() {

        when(asylumCase.read(UPLOAD_REMITTAL_DECISION_DOC, Document.class))
            .thenReturn(Optional.of(remittalDocument));
        when(asylumCase.read(COURT_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(courtRefNumber));
        when(asylumCase.read(SOURCE_OF_REMITTAL, SourceOfRemittal.class))
            .thenReturn(Optional.of(SourceOfRemittal.UPPER_TRIBUNAL));
        when(dateProvider.now()).thenReturn(now);


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            markAppealAsRemittedUploadDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        verify(asylumCase, times(1))
            .read(UPLOAD_REMITTAL_DECISION_DOC, Document.class);
        verify(asylumCase, times(1))
            .write(UPLOAD_REMITTAL_DECISION_DOC,
                new Document("http://localhost/documents/123456", "http://localhost/documents/123456", "CA-000001-Decision-to-remit.pdf"));
        verify(asylumCase, times(1)).write(REHEARING_REASON, "Remitted");
        verify(asylumCase, times(1)).write(APPEAL_REMITTED_DATE, now.toString());
    }

    @Test
    void should_add_remitted_case_note() {

        final List<CaseNote> existingCaseNotes = new ArrayList<>();

        when(asylumCase.read(UPLOAD_REMITTAL_DECISION_DOC, Document.class))
            .thenReturn(Optional.of(remittalDocument));
        when(asylumCase.read(COURT_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(courtRefNumber));
        when(asylumCase.read(SOURCE_OF_REMITTAL, SourceOfRemittal.class))
            .thenReturn(Optional.of(SourceOfRemittal.COURT_OF_APPEAL));
        when(asylumCase.read(JUDGES_NAMES_TO_EXCLUDE, String.class)).thenReturn(Optional.of(judgeExcluded));
        when(asylumCase.read(REMITTED_ADDITIONAL_INSTRUCTIONS, String.class)).thenReturn(Optional.of(additionalInstruction));
        when(dateProvider.now()).thenReturn(now);
        when(asylumCase.read(CASE_NOTES)).thenReturn(Optional.of(existingCaseNotes));
        when(caseNoteAppender.append(any(CaseNote.class), anyList())).thenReturn(allAppendedCaseNotes);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            markAppealAsRemittedUploadDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        verify(caseNoteAppender, times(1)).append(
            newCaseNoteCaptor.capture(),
            existingCaseNotesCaptor.capture());

        CaseNote capturedCaseNote = newCaseNoteCaptor.getValue();

        assertThat(capturedCaseNote.getCaseNoteSubject()).isEqualTo("Appeal marked as remitted");
        assertThat(capturedCaseNote.getUser()).isEqualTo("Admin");
        assertThat(capturedCaseNote.getCaseNoteDescription())
            .isEqualTo("Reason for rehearing: Remitted" + System.lineSeparator() +
                       "Remitted from: Court of Appeal" + System.lineSeparator() +
                       "Court of Appeal reference: CA-000001" + System.lineSeparator() +
                       "Excluded judges: Judge Example" + System.lineSeparator() +
                       "Listing instructions: Additional instruction example" + System.lineSeparator());
        assertThat(capturedCaseNote.getDateAdded()).isEqualTo(now.toString());

        verify(asylumCase, times(1)).write(CASE_NOTES, allAppendedCaseNotes);
    }

    @Test
    void should_throw_on_missing_remittal_decision() {
        when(asylumCase.read(UPLOAD_REMITTAL_DECISION_DOC, Document.class))
            .thenReturn(Optional.empty());

        Assertions
            .assertThatThrownBy(() -> markAppealAsRemittedUploadDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("uploadRemittalDecisionDoc is not present");
    }

    @Test
    void should_throw_on_missing_court_reference_number() {
        when(asylumCase.read(UPLOAD_REMITTAL_DECISION_DOC, Document.class))
            .thenReturn(Optional.of(remittalDocument));


        Assertions
            .assertThatThrownBy(() -> markAppealAsRemittedUploadDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Court reference number not present");
    }

    @Test
    void should_throw_on_missing_source_of_remittal() {
        when(asylumCase.read(UPLOAD_REMITTAL_DECISION_DOC, Document.class))
            .thenReturn(Optional.of(remittalDocument));
        when(asylumCase.read(COURT_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(courtRefNumber));
        when(dateProvider.now()).thenReturn(now);

        Assertions
            .assertThatThrownBy(() -> markAppealAsRemittedUploadDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("sourceOfRemittal is not present");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> markAppealAsRemittedUploadDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> markAppealAsRemittedUploadDecisionHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> markAppealAsRemittedUploadDecisionHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markAppealAsRemittedUploadDecisionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markAppealAsRemittedUploadDecisionHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> markAppealAsRemittedUploadDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = markAppealAsRemittedUploadDecisionHandler.canHandle(callbackStage, callback);

                if ((event == MARK_APPEAL_AS_REMITTED)
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
     void  when_All_Information_Present_Set_Flag() {

        when(asylumCase.read(UPLOAD_REMITTAL_DECISION_DOC, Document.class)).thenReturn(Optional.of(remittalDocument));
        when(dateProvider.now()).thenReturn(LocalDate.now());
        when(asylumCase.read(COURT_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(courtRefNumber));
        when(asylumCase.read(SOURCE_OF_REMITTAL, SourceOfRemittal.class)).thenReturn(Optional.of(SourceOfRemittal.COURT_OF_APPEAL));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = markAppealAsRemittedUploadDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);

        verify(asylumCase).write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YES);
    }

    void should_add_documents_in_collection_and_casefield() {
        List<IdValue<DocumentWithDescription>> allOtherRemittalDocs =
            Arrays.asList(
                new IdValue<>("1", remittalDoc1),
                new IdValue<>("2", remittalDoc2)
            );
        //no documents available in the case field initially
        when(asylumCase.read(REMITTAL_DOCUMENTS)).thenReturn(Optional.empty());
        when(asylumCase.read(UPLOAD_REMITTAL_DECISION_DOC, Document.class)).thenReturn(Optional.of(remittalDocument));
        when(asylumCase.read(UPLOAD_OTHER_REMITTAL_DOCS)).thenReturn(Optional.of(allOtherRemittalDocs));
        when(remittalDocumentsAppender.append(any(), anyList())).thenReturn(remittalDocuments);
        when(asylumCase.read(UPLOAD_REMITTAL_DECISION_DOC, Document.class))
            .thenReturn(Optional.of(remittalDocument));
        when(asylumCase.read(COURT_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(courtRefNumber));
        when(asylumCase.read(SOURCE_OF_REMITTAL, SourceOfRemittal.class))
            .thenReturn(Optional.of(SourceOfRemittal.UPPER_TRIBUNAL));
        when(dateProvider.now()).thenReturn(now);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            markAppealAsRemittedUploadDecisionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        verify(asylumCase, times(1))
            .read(UPLOAD_REMITTAL_DECISION_DOC, Document.class);
        verify(asylumCase, times(1))
            .write(UPLOAD_REMITTAL_DECISION_DOC,
                new Document("http://localhost/documents/123456", "http://localhost/documents/123456","CA-000001-Decision-to-remit.pdf"));
        verify(asylumCase, times(1)).write(REHEARING_REASON, "Remitted");
        verify(asylumCase, times(1)).write(APPEAL_REMITTED_DATE, now.toString());
        verify(asylumCase, times(1)).write(REMITTAL_DOCUMENTS, remittalDocuments);
    }
}
