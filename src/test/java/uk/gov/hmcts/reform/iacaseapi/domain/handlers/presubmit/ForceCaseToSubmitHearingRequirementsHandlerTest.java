package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ForceCaseToSubmitHearingRequirementsHandlerTest {

    @Mock
    private Appender<CaseNote> caseNoteAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DateProvider dateProvider;
    @Mock private UserDetailsProvider userProvider;
    @Mock private CaseNote existingCaseNote;
    @Mock private List allAppendedCaseNotes;
    @Mock private UserDetails userDetails;

    @Captor
    private ArgumentCaptor<List<IdValue<CaseNote>>> existingCaseNotesCaptor;
    @Captor private ArgumentCaptor<CaseNote> newCaseNoteCaptor;

    private final LocalDate now = LocalDate.now();
    private final List<CaseNote> existingCaseNotes = singletonList(existingCaseNote);
    private final String newCaseNoteSubject = "Reason for forcing the case progression to submit hearing requirements";
    private final String newCaseNoteDescription = "some-reason";
    private final String forename = "Frank";
    private final String surname = "Butcher";

    private ForceCaseToSubmitHearingRequirementsHandler forceCaseToSubmitHearingRequirementsHandler;

    @Before
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(userProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getForename()).thenReturn(forename);
        when(userDetails.getSurname()).thenReturn(surname);

        when(dateProvider.now()).thenReturn(now);

        when(asylumCase.read(CASE_NOTES)).thenReturn(Optional.of(existingCaseNotes));
        when(asylumCase.read(REASON_TO_FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS, String.class)).thenReturn(Optional.of(newCaseNoteDescription));

        when(caseNoteAppender.append(any(CaseNote.class), anyList()))
            .thenReturn(allAppendedCaseNotes);

        forceCaseToSubmitHearingRequirementsHandler =
            new ForceCaseToSubmitHearingRequirementsHandler(
                caseNoteAppender,
                dateProvider,
                userProvider
            );
    }

    @Test
    public void should_append_new_case_note_to_existing_case_notes() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            forceCaseToSubmitHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);


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

        verify(asylumCase, times(1)).clear(REASON_TO_FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS);

        assertThat(callbackResponse.getData()).isEqualTo(callbackResponse.getData());
    }

    @Test
    public void should_throw_when_force_case_reason_is_not_present() {

        when(asylumCase.read(REASON_TO_FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forceCaseToSubmitHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("reasonToForceCaseToSubmitHearingRequirements is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> forceCaseToSubmitHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> forceCaseToSubmitHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = forceCaseToSubmitHearingRequirementsHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && event.equals(Event.FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS)) {

                    assertThat(canHandle).isEqualTo(true);
                } else {
                    assertThat(canHandle).isEqualTo(false);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> forceCaseToSubmitHearingRequirementsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> forceCaseToSubmitHearingRequirementsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> forceCaseToSubmitHearingRequirementsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> forceCaseToSubmitHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
