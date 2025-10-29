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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StatutoryTimeframe24Weeks;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.UpdateStatutoryTimeframe24WeeksHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

import java.time.LocalDateTime;
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
class UpdateStatutoryTimeframe24WeeksHandlerTest {

    @Mock
    private Appender<StatutoryTimeframe24Weeks> statutoryTimeframe24WeeksAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DateProvider dateProvider;
    @Mock private StatutoryTimeframe24Weeks statutoryTimeframe24Weeks;
    @Mock private List allAppendedStatutoryTimeframe24Weeks;
    @Mock private UserDetails userDetails;

    @Captor private ArgumentCaptor<List<IdValue<StatutoryTimeframe24Weeks>>> existingStatutoryTimeframe24WeeksCaptor;
    @Captor private ArgumentCaptor<StatutoryTimeframe24Weeks> newStatutoryTimeframe24WeeksCaptor;

    private final List<StatutoryTimeframe24Weeks> existingStatutoryTimeframe24Weeks = singletonList(statutoryTimeframe24Weeks);
    private final LocalDateTime nowWithTime = LocalDateTime.now();
    private final YesOrNo newStatutoryTimeframe24WeeksStatus = YesOrNo.YES;
    private final String newStatutoryTimeframe24WeeksReason = "some-reason";
    private final String forename = "Frank";
    private final String surname = "Butcher";
    private UpdateStatutoryTimeframe24WeeksHandler updateStatutoryTimeframe24WeeksHandler;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPDATE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(userDetails.getForename()).thenReturn(forename);
        when(userDetails.getSurname()).thenReturn(surname);

        when(dateProvider.nowWithTime()).thenReturn(nowWithTime);

        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS)).thenReturn(Optional.of(existingStatutoryTimeframe24Weeks));
        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS_STATUS, YesOrNo.class)).thenReturn(Optional.of(newStatutoryTimeframe24WeeksStatus));
        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS_REASON, String.class)).thenReturn(Optional.of(newStatutoryTimeframe24WeeksReason));

        when(statutoryTimeframe24WeeksAppender.append(any(StatutoryTimeframe24Weeks.class), anyList()))
            .thenReturn(allAppendedStatutoryTimeframe24Weeks);

        updateStatutoryTimeframe24WeeksHandler =
            new UpdateStatutoryTimeframe24WeeksHandler(
                statutoryTimeframe24WeeksAppender,
                dateProvider,
                userDetails
            );
    }

    @Test
    void should_append_new_statutory_timeframe_24_weeks_to_existing_statutory_timeframe_24_weeks() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            updateStatutoryTimeframe24WeeksHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);


        verify(statutoryTimeframe24WeeksAppender, times(1)).append(
            newStatutoryTimeframe24WeeksCaptor.capture(),
            existingStatutoryTimeframe24WeeksCaptor.capture());

        StatutoryTimeframe24Weeks capturedStatutoryTimeframe24Weeks = newStatutoryTimeframe24WeeksCaptor.getValue();

        assertThat(capturedStatutoryTimeframe24Weeks.getStatus()).isEqualTo(YesOrNo.YES);
        assertThat(capturedStatutoryTimeframe24Weeks.getReason()).isEqualTo(newStatutoryTimeframe24WeeksReason);
        assertThat(capturedStatutoryTimeframe24Weeks.getUser()).isEqualTo(forename + " " + surname);
        assertThat(capturedStatutoryTimeframe24Weeks.getDateAdded()).isEqualTo(nowWithTime.toString());

        assertThat(existingStatutoryTimeframe24WeeksCaptor.getValue()).isEqualTo(existingStatutoryTimeframe24Weeks);

        verify(asylumCase, times(1)).write(STATUTORY_TIMEFRAME_24_WEEKS, allAppendedStatutoryTimeframe24Weeks);

        verify(asylumCase, times(1)).clear(STATUTORY_TIMEFRAME_24_WEEKS_STATUS);
        verify(asylumCase, times(1)).clear(STATUTORY_TIMEFRAME_24_WEEKS_REASON);

        assertThat(callbackResponse.getData()).isEqualTo(callbackResponse.getData());
    }

    @Test
    void should_throw_when_statutory_timeframe_24_weeks_status_is_not_present() {

        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS_STATUS, YesOrNo.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateStatutoryTimeframe24WeeksHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("statutoryTimeframe24WeeksStatus is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_statutory_timeframe_24_weeks_reason_is_not_present() {

        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS_REASON, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateStatutoryTimeframe24WeeksHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("statutoryTimeframe24WeeksReason is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> updateStatutoryTimeframe24WeeksHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> updateStatutoryTimeframe24WeeksHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = updateStatutoryTimeframe24WeeksHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && event.equals(Event.UPDATE_STATUTORY_TIMEFRAME_24_WEEKS)) {

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

        assertThatThrownBy(() -> updateStatutoryTimeframe24WeeksHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateStatutoryTimeframe24WeeksHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateStatutoryTimeframe24WeeksHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateStatutoryTimeframe24WeeksHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
