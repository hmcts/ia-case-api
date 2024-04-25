package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OtherDetentionFacilityName;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;



@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UpdateDetentionLocationCaseNoteHandlerTest {

    @Mock
    private Appender<CaseNote> caseNoteAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private CaseDetails<AsylumCase> caseDetailsBefore;

    @Mock private AsylumCase asylumCase;
    @Mock private AsylumCase asylumCaseBefore;

    @Mock private DateProvider dateProvider;
    @Mock private CaseNote existingCaseNote;
    @Mock private List allAppendedCaseNotes;
    @Mock private UserDetails userDetails;
    @Captor private ArgumentCaptor<List<IdValue<CaseNote>>> existingCaseNotesCaptor;
    @Captor private ArgumentCaptor<CaseNote> newCaseNoteCaptor;

    private final List<CaseNote> existingCaseNotes = singletonList(existingCaseNote);
    private final LocalDate now = LocalDate.now();
    private final String newCaseNoteDescription = "some-description";
    private final String forename = "Frank";
    private final String surname = "Butcher";
    private final String newCaseNoteSubject = "Updated detention location";
    private UpdateDetentionLocationCaseNoteHandler updateDetentionLocationCaseNoteHandler;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(callback.getEvent()).thenReturn(Event.UPDATE_DETENTION_LOCATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(userDetails.getForename()).thenReturn(forename);
        when(userDetails.getSurname()).thenReturn(surname);

        when(dateProvider.now()).thenReturn(now);

        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        when(asylumCase.read(PRISON_NAME, String.class)).thenReturn(Optional.of("Aylesbury"));

        when(asylumCaseBefore.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(asylumCaseBefore.read(IRC_NAME, String.class)).thenReturn(Optional.of("Dungavel"));


        when(asylumCase.read(CASE_NOTES)).thenReturn(Optional.of(existingCaseNotes));
        when(asylumCase.read(ADD_CASE_NOTE_SUBJECT, String.class)).thenReturn(Optional.of(newCaseNoteSubject));
        when(asylumCase.read(ADD_CASE_NOTE_DESCRIPTION, String.class)).thenReturn(Optional.of(newCaseNoteDescription));

        when(caseNoteAppender.append(any(CaseNote.class), anyList()))
            .thenReturn(allAppendedCaseNotes);

        updateDetentionLocationCaseNoteHandler =
            new UpdateDetentionLocationCaseNoteHandler(
                caseNoteAppender,
                dateProvider,
                userDetails
            );
    }

    @Test
    void should_append_new_case_note_to_existing_case_notes() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            updateDetentionLocationCaseNoteHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(caseNoteAppender, times(1)).append(
            newCaseNoteCaptor.capture(),
            existingCaseNotesCaptor.capture());

        CaseNote capturedCaseNote = newCaseNoteCaptor.getValue();

        assertThat(capturedCaseNote.getCaseNoteSubject()).isEqualTo(newCaseNoteSubject);
        assertThat(capturedCaseNote.getUser()).isEqualTo(forename + " " + surname);
        assertThat(capturedCaseNote.getDateAdded()).isEqualTo(now.toString());
        assertThat(capturedCaseNote.getCaseNoteDescription()).isEqualTo("The detention location for this appellant has changed from: \n\n"
                        + "Dungavel IRC\n\n to: \n\nAylesbury Prison");

        assertThat(existingCaseNotesCaptor.getValue()).isEqualTo(existingCaseNotes);

        verify(asylumCase, times(1)).write(CASE_NOTES, allAppendedCaseNotes);

        assertThat(callbackResponse.getData()).isEqualTo(callbackResponse.getData());
    }

    @ParameterizedTest
    @ValueSource(strings = { "prison", "immigrationRemovalCentre", "other" })
    void should_throw_when_facility_name_is_not_present(String facilityType) {

        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(facilityType));

        switch (facilityType) {
            case "immigrationRemovalCentre":
                when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.empty());
                assertThatThrownBy(() -> updateDetentionLocationCaseNoteHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                        .hasMessage("IRC_NAME is missing")
                        .isExactlyInstanceOf(RequiredFieldMissingException.class);
                break;
            case "prison":
                when(asylumCase.read(PRISON_NAME, String.class)).thenReturn(Optional.empty());
                assertThatThrownBy(() -> updateDetentionLocationCaseNoteHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                        .hasMessage("PRISON_NAME is missing")
                        .isExactlyInstanceOf(RequiredFieldMissingException.class);
                break;
            case "other":
                when(asylumCase.read(OTHER_DETENTION_FACILITY_NAME, OtherDetentionFacilityName.class)).thenReturn(Optional.empty());
                assertThatThrownBy(() -> updateDetentionLocationCaseNoteHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                        .hasMessage("Other detention facility name is missing")
                        .isExactlyInstanceOf(RequiredFieldMissingException.class);
                break;
            default: break;
        }
        reset(callback);
    }

    @Test
    void should_throw_when_facility_type_is_not_present() {

        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateDetentionLocationCaseNoteHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Detention Facility is missing")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> updateDetentionLocationCaseNoteHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> updateDetentionLocationCaseNoteHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = updateDetentionLocationCaseNoteHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && event.equals(Event.UPDATE_DETENTION_LOCATION)) {

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

        assertThatThrownBy(() -> updateDetentionLocationCaseNoteHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateDetentionLocationCaseNoteHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateDetentionLocationCaseNoteHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateDetentionLocationCaseNoteHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
