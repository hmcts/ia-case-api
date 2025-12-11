package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STATUTORY_TIMEFRAME_24_WEEKS_REASON;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UpdateStatutoryTimeframe24WeeksServiceTest {

    @Mock
    private Appender<StatutoryTimeframe24WeeksHistory> statutoryTimeframe24WeeksHistoryAppender;
    @Mock
    private Appender<CaseNote> caseNoteAppender;
    @Mock private AsylumCase asylumCase;
    @Mock private DateProvider dateProvider;
    @Mock private StatutoryTimeframe24Weeks statutoryTimeframe24Weeks;
    @Mock private List allAppendedStatutoryTimeframe24Weeks;
    @Mock private List allAppendedCaseNotes;
    @Mock private UserDetails userDetails;
    @Mock private BannerTextService bannerTextService;

    @Captor private ArgumentCaptor<List<IdValue<StatutoryTimeframe24WeeksHistory>>> existingStatutoryTimeframe24WeeksHistoryCaptor;
    @Captor private ArgumentCaptor<StatutoryTimeframe24WeeksHistory> newStatutoryTimeframe24WeeksHistoryCaptor;
    @Captor private ArgumentCaptor<List<IdValue<CaseNote>>> existingCaseNotesCaptor;
    @Captor private ArgumentCaptor<CaseNote> newCaseNotesCaptor;
    @Captor private ArgumentCaptor<StatutoryTimeframe24Weeks> statutoryTimeframe24WeeksCaptor;
    @Captor private ArgumentCaptor<AsylumCase> asylumCaseCaptor;

    private final LocalDate now = LocalDate.now();
    private final LocalDateTime nowWithTime = LocalDateTime.now();
    private final String newStatutoryTimeframe24WeeksReason = "some-reason";
    private final String forename = "Frank";
    private final String surname = "Butcher";
    private UpdateStatutoryTimeframe24WeeksService updateStatutoryTimeframe24WeeksService;

    @BeforeEach
    public void setUp() {
        when(userDetails.getForename()).thenReturn(forename);
        when(userDetails.getSurname()).thenReturn(surname);

        when(dateProvider.nowWithTime()).thenReturn(nowWithTime);

        when(statutoryTimeframe24WeeksHistoryAppender.append(any(StatutoryTimeframe24WeeksHistory.class), anyList()))
            .thenReturn(allAppendedStatutoryTimeframe24Weeks);

        when(caseNoteAppender.append(any(CaseNote.class), anyList()))
            .thenReturn(allAppendedCaseNotes);

        updateStatutoryTimeframe24WeeksService =
            new UpdateStatutoryTimeframe24WeeksService(
                statutoryTimeframe24WeeksHistoryAppender,
                caseNoteAppender,
                dateProvider,
                userDetails, bannerTextService);
    }

    @Test
    void should_append_new_statutory_timeframe_24_weeks_to_existing_statutory_timeframe_24_weeks() {
        YesOrNo currentStatus = YesOrNo.NO;
        StatutoryTimeframe24WeeksHistory statutoryTimeframe24WeeksHistory = new StatutoryTimeframe24WeeksHistory(currentStatus, newStatutoryTimeframe24WeeksReason, forename + " " + surname, nowWithTime.toString());
        List<IdValue<StatutoryTimeframe24WeeksHistory>> existingStatutoryTimeframe24WeeksHistory = Arrays.asList(new IdValue<>("1", statutoryTimeframe24WeeksHistory));
        StatutoryTimeframe24Weeks existingStatutoryTimeframe24Weeks = new StatutoryTimeframe24Weeks(currentStatus, existingStatutoryTimeframe24WeeksHistory);

        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS)).thenReturn(Optional.of(existingStatutoryTimeframe24Weeks));
        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS_REASON, String.class)).thenReturn(Optional.of(newStatutoryTimeframe24WeeksReason));

        YesOrNo newStatus = YesOrNo.YES;
        updateStatutoryTimeframe24WeeksService.updateAsylumCase(asylumCase, newStatus);

        verify(statutoryTimeframe24WeeksHistoryAppender, times(1)).append(
            newStatutoryTimeframe24WeeksHistoryCaptor.capture(),
            existingStatutoryTimeframe24WeeksHistoryCaptor.capture());

        StatutoryTimeframe24WeeksHistory capturedStatutoryTimeframe24Weeks = newStatutoryTimeframe24WeeksHistoryCaptor.getValue();

        assertThat(capturedStatutoryTimeframe24Weeks.getStatus()).isEqualTo(newStatus);
        assertThat(capturedStatutoryTimeframe24Weeks.getReason()).isEqualTo(newStatutoryTimeframe24WeeksReason);
        assertThat(capturedStatutoryTimeframe24Weeks.getUser()).isEqualTo(forename + " " + surname);
        assertThat(capturedStatutoryTimeframe24Weeks.getDateTimeAdded()).isEqualTo(nowWithTime.toString());

        verify(caseNoteAppender, times(1)).append(
            newCaseNotesCaptor.capture(),
            existingCaseNotesCaptor.capture());

        CaseNote capturedCaseNotes = newCaseNotesCaptor.getValue();

        assertThat(capturedCaseNotes.getCaseNoteSubject()).isEqualTo("Setting statutory timeframe 24 weeks to " + newStatus);
        assertThat(capturedCaseNotes.getCaseNoteDescription()).isEqualTo(newStatutoryTimeframe24WeeksReason);
        assertThat(capturedCaseNotes.getUser()).isEqualTo(forename + " " + surname);
        assertThat(capturedCaseNotes.getDateAdded()).isEqualTo(now.toString());

        verify(asylumCase, times(1)).write(STATUTORY_TIMEFRAME_24_WEEKS, new StatutoryTimeframe24Weeks(newStatus, allAppendedStatutoryTimeframe24Weeks));
        verify(asylumCase, times(1)).clear(STATUTORY_TIMEFRAME_24_WEEKS_REASON);
        verify(bannerTextService, times(1)).updateBannerText(asylumCaseCaptor.capture(), statutoryTimeframe24WeeksCaptor.capture());
    }

    @Test
    void should_append_new_statutory_timeframe_24_weeks_with_status_NO_to_existing_statutory_timeframe_24_weeks() {
        YesOrNo currentStatus = YesOrNo.YES;
        StatutoryTimeframe24WeeksHistory statutoryTimeframe24WeeksHistory = new StatutoryTimeframe24WeeksHistory(currentStatus, newStatutoryTimeframe24WeeksReason, forename + " " + surname, nowWithTime.toString());
        List<IdValue<StatutoryTimeframe24WeeksHistory>> existingStatutoryTimeframe24WeeksHistory = Arrays.asList(new IdValue<>("1", statutoryTimeframe24WeeksHistory));
        StatutoryTimeframe24Weeks existingStatutoryTimeframe24Weeks = new StatutoryTimeframe24Weeks(currentStatus, existingStatutoryTimeframe24WeeksHistory);

        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS)).thenReturn(Optional.of(existingStatutoryTimeframe24Weeks));
        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS_REASON, String.class)).thenReturn(Optional.of(newStatutoryTimeframe24WeeksReason));

        YesOrNo newStatus = YesOrNo.NO;
        updateStatutoryTimeframe24WeeksService.updateAsylumCase(asylumCase, newStatus);

        verify(statutoryTimeframe24WeeksHistoryAppender, times(1)).append(
            newStatutoryTimeframe24WeeksHistoryCaptor.capture(),
            existingStatutoryTimeframe24WeeksHistoryCaptor.capture());

        StatutoryTimeframe24WeeksHistory capturedStatutoryTimeframe24Weeks = newStatutoryTimeframe24WeeksHistoryCaptor.getValue();

        assertThat(capturedStatutoryTimeframe24Weeks.getStatus()).isEqualTo(newStatus);
        assertThat(capturedStatutoryTimeframe24Weeks.getReason()).isEqualTo(newStatutoryTimeframe24WeeksReason);
        assertThat(capturedStatutoryTimeframe24Weeks.getUser()).isEqualTo(forename + " " + surname);
        assertThat(capturedStatutoryTimeframe24Weeks.getDateTimeAdded()).isEqualTo(nowWithTime.toString());

        verify(caseNoteAppender, times(1)).append(
            newCaseNotesCaptor.capture(),
            existingCaseNotesCaptor.capture());

        CaseNote capturedCaseNotes = newCaseNotesCaptor.getValue();

        assertThat(capturedCaseNotes.getCaseNoteSubject()).isEqualTo("Setting statutory timeframe 24 weeks to " + newStatus);
        assertThat(capturedCaseNotes.getCaseNoteDescription()).isEqualTo(newStatutoryTimeframe24WeeksReason);
        assertThat(capturedCaseNotes.getUser()).isEqualTo(forename + " " + surname);
        assertThat(capturedCaseNotes.getDateAdded()).isEqualTo(now.toString());

        verify(asylumCase, times(1)).write(STATUTORY_TIMEFRAME_24_WEEKS, new StatutoryTimeframe24Weeks(newStatus, allAppendedStatutoryTimeframe24Weeks));
        verify(asylumCase, times(1)).clear(STATUTORY_TIMEFRAME_24_WEEKS_REASON);
    }

    @Test
    void should_throw_when_statutory_timeframe_24_weeks_reason_is_not_present() {
        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS_REASON, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateStatutoryTimeframe24WeeksService.updateAsylumCase(asylumCase, YesOrNo.YES))
            .hasMessage("statutoryTimeframe24WeeksReason is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_statutory_timeframe_24_weeks_status_is_already_set_to_yes() {
        YesOrNo currentStatus = YesOrNo.YES;
        StatutoryTimeframe24WeeksHistory statutoryTimeframe24WeeksHistory = new StatutoryTimeframe24WeeksHistory(currentStatus, newStatutoryTimeframe24WeeksReason, forename + " " + surname, nowWithTime.toString());
        List<IdValue<StatutoryTimeframe24WeeksHistory>> existingStatutoryTimeframe24WeeksHistory = Arrays.asList(new IdValue<>("1", statutoryTimeframe24WeeksHistory));
        StatutoryTimeframe24Weeks existingStatutoryTimeframe24Weeks = new StatutoryTimeframe24Weeks(currentStatus, existingStatutoryTimeframe24WeeksHistory);

        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS)).thenReturn(Optional.of(existingStatutoryTimeframe24Weeks));
        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS_REASON, String.class)).thenReturn(Optional.of(newStatutoryTimeframe24WeeksReason));

        assertThatThrownBy(() -> updateStatutoryTimeframe24WeeksService.updateAsylumCase(asylumCase, currentStatus))
            .hasMessage("The current status is already set to " + currentStatus)
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_statutory_timeframe_24_weeks_status_is_already_set_to_no() {
        YesOrNo currentStatus = YesOrNo.NO;
        StatutoryTimeframe24WeeksHistory statutoryTimeframe24WeeksHistory = new StatutoryTimeframe24WeeksHistory(currentStatus, newStatutoryTimeframe24WeeksReason, forename + " " + surname, nowWithTime.toString());
        List<IdValue<StatutoryTimeframe24WeeksHistory>> existingStatutoryTimeframe24WeeksHistory = Arrays.asList(new IdValue<>("1", statutoryTimeframe24WeeksHistory));
        StatutoryTimeframe24Weeks existingStatutoryTimeframe24Weeks = new StatutoryTimeframe24Weeks(currentStatus, existingStatutoryTimeframe24WeeksHistory);

        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS)).thenReturn(Optional.of(existingStatutoryTimeframe24Weeks));
        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS_REASON, String.class)).thenReturn(Optional.of(newStatutoryTimeframe24WeeksReason));

        assertThatThrownBy(() -> updateStatutoryTimeframe24WeeksService.updateAsylumCase(asylumCase, currentStatus))
            .hasMessage("The current status is already set to " + currentStatus)
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
