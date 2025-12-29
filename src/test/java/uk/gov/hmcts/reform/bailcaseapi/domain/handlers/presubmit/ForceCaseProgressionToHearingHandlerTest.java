package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ForceCaseProgressionToHearingHandlerTest {

    @Mock
    private Appender<CaseNote> caseNoteAppender;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private IdValue<CaseNote> existingCaseNote;
    @Mock
    private List<IdValue<CaseNote>> allAppendedCaseNotes;
    @Mock
    private UserDetails userDetails;

    @Captor
    private ArgumentCaptor<List<IdValue<CaseNote>>> existingCaseNotesCaptor;
    @Captor
    private ArgumentCaptor<CaseNote> newCaseNoteCaptor;

    private final LocalDate now = LocalDate.now();
    private final List<IdValue<CaseNote>> existingCaseNotes = singletonList(existingCaseNote);
    private final String newCaseNoteDescription = "some-reason";
    private final String forename = "Frank";
    private final String surname = "Butcher";

    private ForceCaseProgressionToHearingHandler forceCaseProgressionToHearingHandler;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.FORCE_CASE_TO_HEARING);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

        when(userDetails.getForename()).thenReturn(forename);
        when(userDetails.getSurname()).thenReturn(surname);

        when(dateProvider.now()).thenReturn(now);

        when(bailCase.read(CASE_NOTES)).thenReturn(Optional.of(existingCaseNotes));
        when(bailCase.read(REASON_TO_FORCE_CASE_TO_HEARING, String.class))
            .thenReturn(Optional.of(newCaseNoteDescription));

        when(caseNoteAppender.append(any(CaseNote.class), anyList()))
            .thenReturn(allAppendedCaseNotes);

        forceCaseProgressionToHearingHandler =
            new ForceCaseProgressionToHearingHandler(
                caseNoteAppender,
                dateProvider,
                userDetails
            );
    }

    @Test
    void should_append_new_case_note_to_existing_case_notes() {

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            forceCaseProgressionToHearingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);


        verify(caseNoteAppender, times(1)).append(
            newCaseNoteCaptor.capture(),
            existingCaseNotesCaptor.capture()
        );

        CaseNote capturedCaseNote = newCaseNoteCaptor.getValue();

        String newCaseNoteSubject = "Reason for forcing case progression to hearing";
        assertThat(capturedCaseNote.getCaseNoteSubject()).isEqualTo(newCaseNoteSubject);
        assertThat(capturedCaseNote.getCaseNoteDescription()).isEqualTo(newCaseNoteDescription);
        assertThat(capturedCaseNote.getUser()).isEqualTo(forename + " " + surname);
        assertThat(capturedCaseNote.getDateAdded()).isEqualTo(now.toString());

        assertThat(existingCaseNotesCaptor.getValue()).isEqualTo(existingCaseNotes);

        verify(bailCase, times(1)).write(CASE_NOTES, allAppendedCaseNotes);
        verify(bailCase, times(1)).clear(REASON_TO_FORCE_CASE_TO_HEARING);
        verify(bailCase, times(1)).write(HAS_CASE_BEEN_FORCED_TO_HEARING, YesOrNo.YES);
        verify(bailCase, times(1)).read(HO_SELECT_IMA_STATUS, YesOrNo.class);
        verify(bailCase, times(1)).write(HO_HAS_IMA_STATUS, YesOrNo.NO);

        assertThat(callbackResponse.getData()).isEqualTo(bailCase);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class)
    void should_set_ho_ima_status_if_ima_enabled_to_existing_case_notes(YesOrNo choice) {
        when(bailCase.read(BailCaseFieldDefinition.IS_IMA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HO_SELECT_IMA_STATUS, YesOrNo.class)).thenReturn(Optional.of(choice));
        forceCaseProgressionToHearingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1)).read(HO_SELECT_IMA_STATUS, YesOrNo.class);
        verify(bailCase, times(1)).write(HO_HAS_IMA_STATUS, choice);
    }

    @Test
    void should_throw_when_force_case_reason_is_not_present() {

        when(bailCase.read(REASON_TO_FORCE_CASE_TO_HEARING, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> forceCaseProgressionToHearingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("reasonToForceCaseToHearing is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> forceCaseProgressionToHearingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.EDIT_BAIL_APPLICATION);
        assertThatThrownBy(
            () -> forceCaseProgressionToHearingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = forceCaseProgressionToHearingHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && event.equals(Event.FORCE_CASE_TO_HEARING)) {

                    assertThat(canHandle).isTrue();
                } else {
                    assertThat(canHandle).isFalse();
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> forceCaseProgressionToHearingHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> forceCaseProgressionToHearingHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> forceCaseProgressionToHearingHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> forceCaseProgressionToHearingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
