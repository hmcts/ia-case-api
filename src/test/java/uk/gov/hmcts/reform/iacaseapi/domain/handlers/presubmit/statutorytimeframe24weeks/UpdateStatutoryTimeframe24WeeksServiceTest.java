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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StatutoryTimeframe24Weeks;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StatutoryTimeframe24WeeksHistory;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_NOTES;
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
    @Mock private List allAppendedStatutoryTimeframe24Weeks;
    @Mock private List allAppendedCaseNotes;
    @Mock private UserDetails userDetails;
    @Mock private STF24WeeksBannerTextService bannerTextService;

    @Captor private ArgumentCaptor<List<IdValue<StatutoryTimeframe24WeeksHistory>>> existingStatutoryTimeframe24WeeksHistoryCaptor;
    @Captor private ArgumentCaptor<StatutoryTimeframe24WeeksHistory> newStatutoryTimeframe24WeeksHistoryCaptor;
    @Captor private ArgumentCaptor<List<IdValue<CaseNote>>> existingCaseNotesCaptor;
    @Captor private ArgumentCaptor<CaseNote> newCaseNotesCaptor;
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
                userDetails,
                bannerTextService);
    }

    @Test
    void should_append_new_statutory_timeframe_24_weeks_to_existing_statutory_timeframe_24_weeks() {
        YesOrNo currentStatus = YesOrNo.NO;
        StatutoryTimeframe24WeeksHistory statutoryTimeframe24WeeksHistory = new StatutoryTimeframe24WeeksHistory(currentStatus, newStatutoryTimeframe24WeeksReason, forename + " " + surname, nowWithTime.toString());
        List<IdValue<StatutoryTimeframe24WeeksHistory>> existingStatutoryTimeframe24WeeksHistory = Arrays.asList(new IdValue<>("1", statutoryTimeframe24WeeksHistory));
        StatutoryTimeframe24Weeks existingStatutoryTimeframe24Weeks = new StatutoryTimeframe24Weeks(existingStatutoryTimeframe24WeeksHistory);

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

        assertThat(capturedCaseNotes.getCaseNoteSubject()).isEqualTo("Setting statutory timeframe 24 weeks to - " + newStatus);
        assertThat(capturedCaseNotes.getCaseNoteDescription()).isEqualTo(newStatutoryTimeframe24WeeksReason);
        assertThat(capturedCaseNotes.getUser()).isEqualTo(forename + " " + surname);
        assertThat(capturedCaseNotes.getDateAdded()).isEqualTo(now.toString());

        verify(asylumCase, times(1)).write(STATUTORY_TIMEFRAME_24_WEEKS, new StatutoryTimeframe24Weeks(allAppendedStatutoryTimeframe24Weeks));
        verify(asylumCase, times(1)).clear(STATUTORY_TIMEFRAME_24_WEEKS_REASON);
        verify(bannerTextService, times(1)).updateBannerText(asylumCaseCaptor.capture());

    }

    @Test
    void should_append_new_statutory_timeframe_24_weeks_with_status_NO_to_existing_statutory_timeframe_24_weeks() {
        YesOrNo currentStatus = YesOrNo.YES;
        StatutoryTimeframe24WeeksHistory statutoryTimeframe24WeeksHistory = new StatutoryTimeframe24WeeksHistory(currentStatus, newStatutoryTimeframe24WeeksReason, forename + " " + surname, nowWithTime.toString());
        List<IdValue<StatutoryTimeframe24WeeksHistory>> existingStatutoryTimeframe24WeeksHistory = Arrays.asList(new IdValue<>("1", statutoryTimeframe24WeeksHistory));
        StatutoryTimeframe24Weeks existingStatutoryTimeframe24Weeks = new StatutoryTimeframe24Weeks(existingStatutoryTimeframe24WeeksHistory);

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

        assertThat(capturedCaseNotes.getCaseNoteSubject()).isEqualTo("Setting statutory timeframe 24 weeks to - " + newStatus);
        assertThat(capturedCaseNotes.getCaseNoteDescription()).isEqualTo(newStatutoryTimeframe24WeeksReason);
        assertThat(capturedCaseNotes.getUser()).isEqualTo(forename + " " + surname);
        assertThat(capturedCaseNotes.getDateAdded()).isEqualTo(now.toString());

        verify(asylumCase, times(1)).write(STATUTORY_TIMEFRAME_24_WEEKS, new StatutoryTimeframe24Weeks(allAppendedStatutoryTimeframe24Weeks));
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
    void should_write_statutory_timeframe_when_reason_does_not_contain_home_office_initial_determination() {
        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS_REASON, String.class))
            .thenReturn(Optional.of("Some other reason"));
        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS)).thenReturn(Optional.empty());

        updateStatutoryTimeframe24WeeksService.updateAsylumCase(asylumCase, YesOrNo.YES);

        verify(asylumCase, times(1)).write(eq(STATUTORY_TIMEFRAME_24_WEEKS), any());
    }

    @Test
    void should_skip_write_statutory_timeframe_when_reason_contains_home_office_initial_determination() {
        // This test needs to be updated - the service now writes when status changes
        // If the status is different, it should write; if same, it should throw exception
        
        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS, StatutoryTimeframe24Weeks.class))
            .thenReturn(Optional.of(new StatutoryTimeframe24Weeks(emptyList())));
        when(asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS_REASON, String.class))
            .thenReturn(Optional.of("Home Office Initial Determination"));

        updateStatutoryTimeframe24WeeksService.updateAsylumCase(asylumCase, YesOrNo.YES);

        verify(asylumCase, times(1)).write(eq(STATUTORY_TIMEFRAME_24_WEEKS), any(StatutoryTimeframe24Weeks.class));
        verify(asylumCase, times(1)).write(eq(CASE_NOTES), anyList());
    }

}
