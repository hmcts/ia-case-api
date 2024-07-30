package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.migratedcase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNoteMigration;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADD_CASE_NOTES_MIGRATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_DESIRED_STATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.PROGRESS_MIGRATED_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.PRE_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ProgressMigratedCaseHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private PreSubmitCallbackResponse<AsylumCase> callbackResponse;
    @Mock
    private Appender<CaseNote> caseNoteAppender;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private UserDetails userDetails;


    private ProgressMigratedCaseHandler progressMigratedCaseHandler;

    @BeforeEach
    public void setUp() {
        progressMigratedCaseHandler = new ProgressMigratedCaseHandler(caseNoteAppender, dateProvider, userDetails);
        when(callback.getEvent()).thenReturn(PROGRESS_MIGRATED_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(123L);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = progressMigratedCaseHandler.canHandle(callbackStage, callback);

                if (callback.getEvent() == Event.PROGRESS_MIGRATED_CASE && callbackStage == ABOUT_TO_SUBMIT) {
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
        assertThatThrownBy(() -> progressMigratedCaseHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> progressMigratedCaseHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> progressMigratedCaseHandler.handle(null, callback, callbackResponse))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> progressMigratedCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null, callbackResponse))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_if_aria_desired_state_is_not_present() {
        when(asylumCase.read(ARIA_DESIRED_STATE, State.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressMigratedCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse))
            .hasMessage("ariaDesiredState is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_change_state_value_from_desired_state() {
        when(asylumCase.read(ARIA_DESIRED_STATE, State.class)).thenReturn(Optional.of(PRE_HEARING));

        PreSubmitCallbackResponse<AsylumCase> response =
            progressMigratedCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertEquals(State.PRE_HEARING, response.getState());
    }

    @Test
    public void should_do_nothing_when_no_existing_case_notes() {
        when(asylumCase.read(ARIA_DESIRED_STATE, State.class)).thenReturn(Optional.of(PRE_HEARING));
        when(asylumCase.read(ADD_CASE_NOTES_MIGRATION)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            progressMigratedCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);
        // Then
        verify(asylumCase, never()).write(CASE_NOTES, emptyList());
        verify(asylumCase, never()).clear(ADD_CASE_NOTES_MIGRATION);

    }

    @Test
    void should_create_case_notes_with_documents() {
        when(asylumCase.read(ARIA_DESIRED_STATE, State.class)).thenReturn(Optional.of(PRE_HEARING));

        String subject1 = "Test Subject 1";
        String subject2 = "Test Subject 2";

        String description1 = "Test Description 1";
        String description2 = "Test Description 2";

        Document document1 = mock(Document.class);
        Document document2 = mock(Document.class);

        String fullName = "Full Name";
        LocalDate currentDate = LocalDate.of(2024, 7, 25);

        CaseNote caseNote1 = new CaseNote(subject1, description1, fullName, currentDate.toString());
        caseNote1.setCaseNoteDocument(document1);
        CaseNote caseNote2 = new CaseNote(subject2, description2, fullName, currentDate.toString());
        caseNote2.setCaseNoteDocument(document2);

        when(asylumCase.read(ADD_CASE_NOTES_MIGRATION)).thenReturn(Optional.of(Arrays.asList(
            new IdValue<>("1", new CaseNoteMigration(subject1, description1, document1)),
            new IdValue<>("2", new CaseNoteMigration(subject2, description2, document2))
        )));

        when(userDetails.getForename()).thenReturn("Full");
        when(userDetails.getSurname()).thenReturn("Name");

        when(dateProvider.now()).thenReturn(currentDate);
        when(caseNoteAppender.append(any(), any())).thenAnswer(invocation -> {
            CaseNote caseNote = invocation.getArgument(0);
            List<IdValue<CaseNote>> list = invocation.getArgument(1);
            list.add(new IdValue<>(list.size() + "", caseNote));
            return list;
        });

        PreSubmitCallbackResponse<AsylumCase> response =
            progressMigratedCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertEquals(State.PRE_HEARING, response.getState());

        ArgumentCaptor<List> writeCaptor = ArgumentCaptor.forClass(List.class);
        verify(asylumCase).write(eq(CASE_NOTES), writeCaptor.capture());
        List<IdValue<CaseNote>> writtenCaseNotes = writeCaptor.getValue();

        assertEquals(2, writtenCaseNotes.size());
        CaseNote writtenCaseNote1 = writtenCaseNotes.get(1).getValue();

        assertEquals(subject1, writtenCaseNote1.getCaseNoteSubject());
        assertEquals(description1, writtenCaseNote1.getCaseNoteDescription());
        assertEquals(document1, writtenCaseNote1.getCaseNoteDocument());
        assertEquals(fullName, writtenCaseNote1.getUser());
        assertEquals(currentDate.toString(), writtenCaseNote1.getDateAdded());

        CaseNote writtenCaseNote2 = writtenCaseNotes.get(0).getValue();

        assertEquals(subject2, writtenCaseNote2.getCaseNoteSubject());
        assertEquals(description2, writtenCaseNote2.getCaseNoteDescription());
        assertEquals(document2, writtenCaseNote2.getCaseNoteDocument());
        assertEquals(fullName, writtenCaseNote2.getUser());
        assertEquals(currentDate.toString(), writtenCaseNote2.getDateAdded());

        verify(asylumCase).clear(ADD_CASE_NOTES_MIGRATION);

    }

    @Test
    void should_create_case_notes_without_documents() {
        when(asylumCase.read(ARIA_DESIRED_STATE, State.class)).thenReturn(Optional.of(PRE_HEARING));

        String subject1 = "Test Subject 1";
        String subject2 = "Test Subject 2";

        String description1 = "Test Description 1";
        String description2 = "Test Description 2";


        String fullName = "Full Name";
        LocalDate currentDate = LocalDate.of(2024, 7, 25);

        CaseNote caseNote1 = new CaseNote(subject1, description1, fullName, currentDate.toString());
        CaseNote caseNote2 = new CaseNote(subject2, description2, fullName, currentDate.toString());

        when(asylumCase.read(ADD_CASE_NOTES_MIGRATION)).thenReturn(Optional.of(Arrays.asList(
            new IdValue<>("1", new CaseNoteMigration(subject1, description1, null)),
            new IdValue<>("2", new CaseNoteMigration(subject2, description2, null))
        )));

        when(userDetails.getForename()).thenReturn("Full");
        when(userDetails.getSurname()).thenReturn("Name");

        when(dateProvider.now()).thenReturn(currentDate);
        when(caseNoteAppender.append(any(), any())).thenAnswer(invocation -> {
            CaseNote caseNote = invocation.getArgument(0);
            List<IdValue<CaseNote>> list = invocation.getArgument(1);
            list.add(new IdValue<>(list.size() + "", caseNote));
            return list;
        });

        PreSubmitCallbackResponse<AsylumCase> response =
            progressMigratedCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertEquals(State.PRE_HEARING, response.getState());

        ArgumentCaptor<List> writeCaptor = ArgumentCaptor.forClass(List.class);
        verify(asylumCase).write(eq(CASE_NOTES), writeCaptor.capture());
        List<IdValue<CaseNote>> writtenCaseNotes = writeCaptor.getValue();

        assertEquals(2, writtenCaseNotes.size());
        CaseNote writtenCaseNote1 = writtenCaseNotes.get(1).getValue();

        assertEquals(subject1, writtenCaseNote1.getCaseNoteSubject());
        assertEquals(description1, writtenCaseNote1.getCaseNoteDescription());
        assertEquals(null, writtenCaseNote1.getCaseNoteDocument());
        assertEquals(fullName, writtenCaseNote1.getUser());
        assertEquals(currentDate.toString(), writtenCaseNote1.getDateAdded());

        CaseNote writtenCaseNote2 = writtenCaseNotes.get(0).getValue();

        assertEquals(subject2, writtenCaseNote2.getCaseNoteSubject());
        assertEquals(description2, writtenCaseNote2.getCaseNoteDescription());
        assertEquals(null, writtenCaseNote2.getCaseNoteDocument());
        assertEquals(fullName, writtenCaseNote2.getUser());
        assertEquals(currentDate.toString(), writtenCaseNote2.getDateAdded());

        verify(asylumCase).clear(ADD_CASE_NOTES_MIGRATION);

    }

}
