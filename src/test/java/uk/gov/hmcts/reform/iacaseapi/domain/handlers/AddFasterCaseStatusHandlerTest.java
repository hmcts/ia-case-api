package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FasterCaseStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.AddFasterCaseStatusHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AddFasterCaseStatusHandlerTest {

    @Mock
    private Appender<FasterCaseStatus> fasterCaseStatusAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DateProvider dateProvider;
    @Mock private FasterCaseStatus existingFasterCaseStatus;
    @Mock private List allAppendedFasterCaseStatuses;
    @Mock private UserDetails userDetails;

    @Captor private ArgumentCaptor<List<IdValue<FasterCaseStatus>>> existingFasterCaseStatusCaptor;
    @Captor private ArgumentCaptor<FasterCaseStatus> newFasterCaseStatusCaptor;

    private final List<FasterCaseStatus> existingFasterCaseStatuses = singletonList(existingFasterCaseStatus);
    private final LocalDate now = LocalDate.now();
    private final YesOrNo newFasterCaseStatus = YesOrNo.YES;
    private final String newFasterCaseStatusReason = "some-reason";
    private final String forename = "Frank";
    private final String surname = "Butcher";
    private AddFasterCaseStatusHandler addFasterCaseStatusHandler;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.ADD_FASTER_CASE_STATUS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(userDetails.getForename()).thenReturn(forename);
        when(userDetails.getSurname()).thenReturn(surname);

        when(dateProvider.now()).thenReturn(now);

        when(asylumCase.read(FASTER_CASE_STATUSES)).thenReturn(Optional.of(existingFasterCaseStatuses));
        when(asylumCase.read(FASTER_CASE_STATUS_STATUS, YesOrNo.class)).thenReturn(Optional.of(newFasterCaseStatus));
        when(asylumCase.read(FASTER_CASE_STATUS_REASON, String.class)).thenReturn(Optional.of(newFasterCaseStatusReason));

        when(fasterCaseStatusAppender.append(any(FasterCaseStatus.class), anyList()))
            .thenReturn(allAppendedFasterCaseStatuses);

        addFasterCaseStatusHandler =
            new AddFasterCaseStatusHandler(
                fasterCaseStatusAppender,
                dateProvider,
                userDetails
            );
    }

    @Test
    void should_append_new_case_note_to_existing_case_notes() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            addFasterCaseStatusHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);


        verify(fasterCaseStatusAppender, times(1)).append(
            newFasterCaseStatusCaptor.capture(),
            existingFasterCaseStatusCaptor.capture());

        FasterCaseStatus capturedCaseNote = newFasterCaseStatusCaptor.getValue();

        assertThat(capturedCaseNote.getFasterCaseStatus()).isEqualTo(YesOrNo.YES);
        assertThat(capturedCaseNote.getFasterCaseStatusReason()).isEqualTo(newFasterCaseStatusReason);
        assertThat(capturedCaseNote.getUser()).isEqualTo(forename + " " + surname);
        assertThat(capturedCaseNote.getDateAdded()).isEqualTo(now.toString());

        assertThat(existingFasterCaseStatusCaptor.getValue()).isEqualTo(existingFasterCaseStatuses);

        verify(asylumCase, times(1)).write(FASTER_CASE_STATUSES, allAppendedFasterCaseStatuses);

        verify(asylumCase, times(1)).clear(FASTER_CASE_STATUS_STATUS);
        verify(asylumCase, times(1)).clear(FASTER_CASE_STATUS_REASON);

        assertThat(callbackResponse.getData()).isEqualTo(callbackResponse.getData());
    }

    @Test
    void should_throw_when_case_note_subject_is_not_present() {

        when(asylumCase.read(FASTER_CASE_STATUS_STATUS, YesOrNo.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addFasterCaseStatusHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("fasterCaseStatus is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_case_note_description_is_not_present() {

        when(asylumCase.read(FASTER_CASE_STATUS_REASON, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addFasterCaseStatusHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("fasterCaseStatusReason is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> addFasterCaseStatusHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> addFasterCaseStatusHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = addFasterCaseStatusHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && event.equals(Event.ADD_FASTER_CASE_STATUS)) {

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

        assertThatThrownBy(() -> addFasterCaseStatusHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addFasterCaseStatusHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addFasterCaseStatusHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addFasterCaseStatusHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
