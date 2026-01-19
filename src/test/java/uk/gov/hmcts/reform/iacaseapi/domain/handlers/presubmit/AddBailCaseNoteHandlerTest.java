package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADD_CASE_NOTE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADD_CASE_NOTE_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADD_CASE_NOTE_SUBJECT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_NOTES;

import java.time.LocalDate;
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
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
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
class AddBailCaseNoteHandlerTest {

    @Mock
    private Appender<CaseNote> caseNoteAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DateProvider dateProvider;
    @Mock private CaseNote existingCaseNote;
    @Mock private List allAppendedCaseNotes;
    @Mock private UserDetails userDetails;
    @Mock private Document newCaseNoteDocument;

    @Captor private ArgumentCaptor<List<IdValue<CaseNote>>> existingCaseNotesCaptor;
    @Captor private ArgumentCaptor<CaseNote> newCaseNoteCaptor;

    private final List<CaseNote> existingCaseNotes = singletonList(existingCaseNote);
    private final LocalDate now = LocalDate.now();
    private final String newCaseNoteSubject = "some-subject";
    private final String newCaseNoteDescription = "some-description";
    private final String forename = "Frank";
    private final String surname = "Butcher";
    private AddBailCaseNoteHandler addBailCaseNoteHandler;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.ADD_CASE_NOTE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(userDetails.getForename()).thenReturn(forename);
        when(userDetails.getSurname()).thenReturn(surname);

        when(dateProvider.now()).thenReturn(now);

        when(asylumCase.read(CASE_NOTES)).thenReturn(Optional.of(existingCaseNotes));
        when(asylumCase.read(ADD_CASE_NOTE_SUBJECT, String.class)).thenReturn(Optional.of(newCaseNoteSubject));
        when(asylumCase.read(ADD_CASE_NOTE_DESCRIPTION, String.class)).thenReturn(Optional.of(newCaseNoteDescription));

        when(caseNoteAppender.append(any(CaseNote.class), anyList()))
            .thenReturn(allAppendedCaseNotes);

        addBailCaseNoteHandler =
            new AddBailCaseNoteHandler(
                caseNoteAppender,
                dateProvider,
                userDetails
            );
    }

    @Test
    void should_append_new_case_note_to_existing_case_notes() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            addBailCaseNoteHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);


        verify(caseNoteAppender, times(1)).append(
            newCaseNoteCaptor.capture(),
            existingCaseNotesCaptor.capture());

        CaseNote capturedCaseNote = newCaseNoteCaptor.getValue();

        assertThat(capturedCaseNote.getCaseNoteSubject()).isEqualTo(newCaseNoteSubject);
        assertThat(capturedCaseNote.getCaseNoteDescription()).isEqualTo(newCaseNoteDescription);
        assertThat(capturedCaseNote.getUser()).isEqualTo(forename + " " + surname);
        assertThat(capturedCaseNote.getDateAdded()).isEqualTo(now.toString());

        assertThat(existingCaseNotesCaptor.getValue()).isEqualTo(existingCaseNotes);

        verify(asylumCase, times(1)).write(CASE_NOTES, allAppendedCaseNotes);

        verify(asylumCase, times(1)).clear(ADD_CASE_NOTE_SUBJECT);
        verify(asylumCase, times(1)).clear(ADD_CASE_NOTE_DESCRIPTION);
        verify(asylumCase, times(1)).clear(ADD_CASE_NOTE_DOCUMENT);

        assertThat(callbackResponse.getData()).isEqualTo(callbackResponse.getData());
    }

    @Test
    void sets_case_document_if_present() {

        when(asylumCase.read(ADD_CASE_NOTE_DOCUMENT, Document.class))
            .thenReturn(Optional.of(newCaseNoteDocument));

        addBailCaseNoteHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(caseNoteAppender, times(1)).append(
            newCaseNoteCaptor.capture(),
            existingCaseNotesCaptor.capture());

        assertThat(newCaseNoteCaptor.getValue().getCaseNoteDocument())
            .isEqualTo(newCaseNoteDocument);
    }

    @Test
    void should_throw_when_case_note_subject_is_not_present() {

        when(asylumCase.read(ADD_CASE_NOTE_SUBJECT, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addBailCaseNoteHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("addCaseNoteSubject is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_case_note_description_is_not_present() {

        when(asylumCase.read(ADD_CASE_NOTE_DESCRIPTION, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addBailCaseNoteHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("addCaseNoteDescription is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> addBailCaseNoteHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> addBailCaseNoteHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = addBailCaseNoteHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && event.equals(Event.ADD_CASE_NOTE)) {

                    assertThat(canHandle).isEqualTo(true);
                } else {
                    assertThat(canHandle).isEqualTo(false);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> addBailCaseNoteHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addBailCaseNoteHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addBailCaseNoteHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addBailCaseNoteHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
