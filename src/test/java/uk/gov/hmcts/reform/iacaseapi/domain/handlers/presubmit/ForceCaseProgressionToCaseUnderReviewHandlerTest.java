package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REASON_TO_FORCE_CASE_TO_CASE_UNDER_REVIEW;

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
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ForceCaseProgressionToCaseUnderReviewHandlerTest {

    @Mock
    Appender<CaseNote> caseNoteAppender;
    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock AsylumCase asylumCase;
    @Mock DateProvider dateProvider;
    @Mock UserDetailsProvider userProvider;
    @Mock CaseNote existingCaseNote;
    @Mock List allAppendedCaseNotes;
    @Mock UserDetails userDetails;

    @Captor
    ArgumentCaptor<List<IdValue<CaseNote>>> existingCaseNotesCaptor;
    @Captor ArgumentCaptor<CaseNote> newCaseNoteCaptor;

    final LocalDate now = LocalDate.now();
    final List<CaseNote> existingCaseNotes = singletonList(existingCaseNote);
    final String newCaseNoteSubject = "Reason for forcing case progression to case under review";
    final String newCaseNoteDescription = "some-reason";
    final String forename = "Frank";
    final String surname = "Butcher";

    ForceCaseProgressionToCaseUnderReviewHandler forceCaseProgressionToCaseUnderReviewHandler;

    @BeforeEach
    void setUp() {

        forceCaseProgressionToCaseUnderReviewHandler =
            new ForceCaseProgressionToCaseUnderReviewHandler(
                caseNoteAppender,
                dateProvider,
                userProvider
            );
    }

    @Test
    void should_append_new_case_note_to_existing_case_notes() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.FORCE_CASE_TO_CASE_UNDER_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(userProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getForename()).thenReturn(forename);
        when(userDetails.getSurname()).thenReturn(surname);

        when(dateProvider.now()).thenReturn(now);

        when(asylumCase.read(CASE_NOTES)).thenReturn(Optional.of(existingCaseNotes));
        when(asylumCase.read(REASON_TO_FORCE_CASE_TO_CASE_UNDER_REVIEW, String.class)).thenReturn(Optional.of(newCaseNoteDescription));

        when(caseNoteAppender.append(any(CaseNote.class), anyList()))
            .thenReturn(allAppendedCaseNotes);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            forceCaseProgressionToCaseUnderReviewHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);


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

        verify(asylumCase, times(1)).clear(REASON_TO_FORCE_CASE_TO_CASE_UNDER_REVIEW);

        assertThat(callbackResponse.getData()).isEqualTo(callbackResponse.getData());
    }

    @Test
    void should_throw_when_force_case_reason_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.FORCE_CASE_TO_CASE_UNDER_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(REASON_TO_FORCE_CASE_TO_CASE_UNDER_REVIEW, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forceCaseProgressionToCaseUnderReviewHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("reasonToForceCaseToCaseUnderReview is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> forceCaseProgressionToCaseUnderReviewHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> forceCaseProgressionToCaseUnderReviewHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = forceCaseProgressionToCaseUnderReviewHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && event.equals(Event.FORCE_CASE_TO_CASE_UNDER_REVIEW)) {

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

        assertThatThrownBy(() -> forceCaseProgressionToCaseUnderReviewHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> forceCaseProgressionToCaseUnderReviewHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> forceCaseProgressionToCaseUnderReviewHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> forceCaseProgressionToCaseUnderReviewHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}