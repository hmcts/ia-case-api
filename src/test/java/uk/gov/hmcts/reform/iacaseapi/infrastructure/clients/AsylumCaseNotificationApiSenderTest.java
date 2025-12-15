package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SAVE_NOTIFICATIONS_TO_DATA_DATE;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class AsylumCaseNotificationApiSenderTest {

    private static final String ENDPOINT = "http://endpoint";
    private static final String CCD_SUBMITTED_PATH = "/path";

    @Mock
    private AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private Scheduler scheduler;

    private AsylumCaseNotificationApiSender asylumCaseNotificationApiSender;

    @Mock
    private FeatureToggler featureToggler;
    @Captor
    private ArgumentCaptor<TimedEvent> timedEventCaptor;

    private static final int SAVE_NOTIFICATIONS_DATA_SCHEDULE_HOUR = 20;
    private static final int SAVE_NOTIFICATIONS_DATA_SCHEDULE_MAX_MINUTES = 10;

    @BeforeEach
    public void setUp() {
        asylumCaseNotificationApiSender =
            new AsylumCaseNotificationApiSender(
                asylumCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH,
                false,
                true,
                SAVE_NOTIFICATIONS_DATA_SCHEDULE_HOUR,
                SAVE_NOTIFICATIONS_DATA_SCHEDULE_MAX_MINUTES,
                dateProvider,
                scheduler,
                featureToggler
            );
        when(featureToggler.getValue("save-notifications-feature", false)).thenReturn(true);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }
    
    @Test
    void should_delegate_callback_to_downstream_api_when_previous_scheduled_time_empty() {
        final AsylumCase notifiedAsylumCase = mock(AsylumCase.class);

        when(asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(notifiedAsylumCase);

        when(asylumCase.read(SAVE_NOTIFICATIONS_TO_DATA_DATE, String.class)).thenReturn(Optional.empty());

        final AsylumCase callbackResponse = asylumCaseNotificationApiSender.send(callback);

        verify(scheduler, never()).schedule(any(TimedEvent.class));
        verify(asylumCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(notifiedAsylumCase, callbackResponse);
    }

    @Test
    void should_delegate_callback_to_downstream_api_when_previous_scheduled_time_today() {
        final AsylumCase notifiedAsylumCase = mock(AsylumCase.class);

        when(asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(notifiedAsylumCase);

        when(asylumCase.read(SAVE_NOTIFICATIONS_TO_DATA_DATE, String.class)).thenReturn(Optional.of(LocalDate.now().toString()));

        final AsylumCase callbackResponse = asylumCaseNotificationApiSender.send(callback);

        verify(scheduler, never()).schedule(any(TimedEvent.class));
        verify(asylumCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(notifiedAsylumCase, callbackResponse);
    }

    @Test
    void should_delegate_callback_to_downstream_api_when_previous_scheduled_time_yesterday() {
        final AsylumCase notifiedAsylumCase = mock(AsylumCase.class);

        when(asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(notifiedAsylumCase);

        when(asylumCase.read(SAVE_NOTIFICATIONS_TO_DATA_DATE, String.class)).thenReturn(Optional.of(LocalDate.now().minusDays(1).toString()));

        final AsylumCase callbackResponse = asylumCaseNotificationApiSender.send(callback);

        verify(scheduler, never()).schedule(any(TimedEvent.class));
        verify(asylumCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(notifiedAsylumCase, callbackResponse);
    }

    @Test
    void should_delegate_about_to_start_callback_to_downstream_api_when_previous_scheduled_time_empty() {
        final AsylumCase notifiedAsylumCase = mock(AsylumCase.class);

        when(asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(notifiedAsylumCase);

        when(asylumCase.read(SAVE_NOTIFICATIONS_TO_DATA_DATE, String.class)).thenReturn(Optional.empty());

        final AsylumCase actualAsylumCase = asylumCaseNotificationApiSender.send(callback);
        verify(scheduler, never()).schedule(any(TimedEvent.class));
        verify(asylumCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        verify(asylumCase).write(SAVE_NOTIFICATIONS_TO_DATA_DATE, LocalDate.now());

        assertEquals(notifiedAsylumCase, actualAsylumCase);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> asylumCaseNotificationApiSender.send(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void schedules_save_notification_to_data_event_if_tes_enabled_and_previous_scheduled_time_empty() {
        asylumCaseNotificationApiSender =
            new AsylumCaseNotificationApiSender(
                asylumCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH,
                true,
                true,
                SAVE_NOTIFICATIONS_DATA_SCHEDULE_HOUR,
                SAVE_NOTIFICATIONS_DATA_SCHEDULE_MAX_MINUTES,
                dateProvider,
                scheduler,
                featureToggler
            );
        final AsylumCase notifiedAsylumCase = mock(AsylumCase.class);

        when(asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(notifiedAsylumCase);
        LocalDateTime localDateTime = LocalDateTime.now();
        when(dateProvider.nowWithTime()).thenReturn(localDateTime);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(1L);

        when(asylumCase.read(SAVE_NOTIFICATIONS_TO_DATA_DATE, String.class)).thenReturn(Optional.empty());

        final AsylumCase callbackResponse = asylumCaseNotificationApiSender.send(callback);

        verify(scheduler).schedule(timedEventCaptor.capture());
        TimedEvent scheduledTimedEventValue = timedEventCaptor.getValue();
        verifyTimedEventSchedule(scheduledTimedEventValue);
        assertEquals(scheduledTimedEventValue.getScheduledDateTime().toLocalDate(), LocalDate.now());

        verify(asylumCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        verify(asylumCase).write(SAVE_NOTIFICATIONS_TO_DATA_DATE, LocalDate.now());

        assertEquals(notifiedAsylumCase, callbackResponse);
    }

    @Test
    void schedules_not_save_notification_to_data_event_if_tes_enabled_and_previous_scheduled_time_today() {
        asylumCaseNotificationApiSender =
            new AsylumCaseNotificationApiSender(
                asylumCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH,
                true,
                true,
                SAVE_NOTIFICATIONS_DATA_SCHEDULE_HOUR,
                SAVE_NOTIFICATIONS_DATA_SCHEDULE_MAX_MINUTES,
                dateProvider,
                scheduler,
                featureToggler
            );
        final AsylumCase notifiedAsylumCase = mock(AsylumCase.class);

        when(asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(notifiedAsylumCase);
        LocalDateTime localDateTime = LocalDateTime.now();
        when(dateProvider.nowWithTime()).thenReturn(localDateTime);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(1L);

        when(asylumCase.read(SAVE_NOTIFICATIONS_TO_DATA_DATE, String.class)).thenReturn(Optional.of(LocalDate.now().toString()));

        final AsylumCase callbackResponse = asylumCaseNotificationApiSender.send(callback);

        verifyNoInteractions(scheduler);

        verify(asylumCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(notifiedAsylumCase, callbackResponse);
    }

    @Test
    void schedules_save_notification_to_data_event_if_tes_enabled_and_previous_scheduled_time_yesterday() {
        asylumCaseNotificationApiSender =
            new AsylumCaseNotificationApiSender(
                asylumCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH,
                true,
                true,
                SAVE_NOTIFICATIONS_DATA_SCHEDULE_HOUR,
                SAVE_NOTIFICATIONS_DATA_SCHEDULE_MAX_MINUTES,
                dateProvider,
                scheduler,
                featureToggler
            );
        final AsylumCase notifiedAsylumCase = mock(AsylumCase.class);

        when(asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(notifiedAsylumCase);
        LocalDateTime localDateTime = LocalDateTime.now();
        when(dateProvider.nowWithTime()).thenReturn(localDateTime);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(1L);

        when(asylumCase.read(SAVE_NOTIFICATIONS_TO_DATA_DATE, String.class)).thenReturn(Optional.of(LocalDate.now().minusDays(1).toString()));

        final AsylumCase callbackResponse = asylumCaseNotificationApiSender.send(callback);

        verify(scheduler).schedule(timedEventCaptor.capture());
        TimedEvent scheduledTimedEventValue = timedEventCaptor.getValue();
        verifyTimedEventSchedule(scheduledTimedEventValue);
        assertEquals(scheduledTimedEventValue.getScheduledDateTime().toLocalDate(), LocalDate.now());

        verify(asylumCase).write(SAVE_NOTIFICATIONS_TO_DATA_DATE, LocalDate.now());

        verify(asylumCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(notifiedAsylumCase, callbackResponse);
    }

    @Test
    void should_schedule_to_next_day_when_tes_enabled_and_previous_scheduled_time_empty_and_current_time_is_past_the_schedule_hour() {
        asylumCaseNotificationApiSender =
                new AsylumCaseNotificationApiSender(
                        asylumCaseCallbackApiDelegator,
                        ENDPOINT,
                        CCD_SUBMITTED_PATH,
                        true,
                        true,
                        SAVE_NOTIFICATIONS_DATA_SCHEDULE_HOUR,
                        SAVE_NOTIFICATIONS_DATA_SCHEDULE_HOUR,
                        dateProvider,
                        scheduler,
                        featureToggler
                );
        final AsylumCase notifiedAsylumCase = mock(AsylumCase.class);

        when(asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
                .thenReturn(notifiedAsylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(1L);
        LocalDateTime afterScheduleHour = LocalDateTime.now().withHour(SAVE_NOTIFICATIONS_DATA_SCHEDULE_HOUR).withMinute(1);
        when(dateProvider.nowWithTime()).thenReturn(afterScheduleHour);

        when(asylumCase.read(SAVE_NOTIFICATIONS_TO_DATA_DATE, String.class)).thenReturn(Optional.empty());

        final AsylumCase callbackResponse = asylumCaseNotificationApiSender.send(callback);

        verify(scheduler).schedule(timedEventCaptor.capture());
        TimedEvent timedEventCaptorValue = timedEventCaptor.getValue();
        verifyTimedEventSchedule(timedEventCaptorValue);
        assertTrue(timedEventCaptorValue.getScheduledDateTime().isAfter(ZonedDateTime.now().plusDays(1)));

        verify(asylumCaseCallbackApiDelegator, times(1))
                .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        verify(asylumCase).write(SAVE_NOTIFICATIONS_TO_DATA_DATE, LocalDate.now());

        assertEquals(notifiedAsylumCase, callbackResponse);
    }

    @Test
    void should_schedule_to_next_day_when_tes_enabled_and_current_time_is_past_the_schedule_hour_previous_scheduled_time_yesterday() {
        asylumCaseNotificationApiSender =
                new AsylumCaseNotificationApiSender(
                        asylumCaseCallbackApiDelegator,
                        ENDPOINT,
                        CCD_SUBMITTED_PATH,
                        true,
                        true,
                        SAVE_NOTIFICATIONS_DATA_SCHEDULE_HOUR,
                        SAVE_NOTIFICATIONS_DATA_SCHEDULE_HOUR,
                        dateProvider,
                        scheduler,
                        featureToggler
                );
        final AsylumCase notifiedAsylumCase = mock(AsylumCase.class);

        when(asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
                .thenReturn(notifiedAsylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(1L);
        LocalDateTime afterScheduleHour = LocalDateTime.now().withHour(SAVE_NOTIFICATIONS_DATA_SCHEDULE_HOUR).withMinute(1);
        when(dateProvider.nowWithTime()).thenReturn(afterScheduleHour);

        when(asylumCase.read(SAVE_NOTIFICATIONS_TO_DATA_DATE, String.class)).thenReturn(Optional.of(LocalDate.now().minusDays(1).toString()));

        final AsylumCase callbackResponse = asylumCaseNotificationApiSender.send(callback);

        verify(scheduler).schedule(timedEventCaptor.capture());
        TimedEvent timedEventCaptorValue = timedEventCaptor.getValue();
        verifyTimedEventSchedule(timedEventCaptorValue);
        assertTrue(timedEventCaptorValue.getScheduledDateTime().isAfter(ZonedDateTime.now().plusDays(1)));

        verify(asylumCaseCallbackApiDelegator, times(1))
                .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        verify(asylumCase).write(SAVE_NOTIFICATIONS_TO_DATA_DATE, LocalDate.now());

        assertEquals(notifiedAsylumCase, callbackResponse);
    }

    @Test
    void should_not_schedule_when_tes_enabled_and_current_time_is_past_the_schedule_hour_previous_scheduled_time_today() {
        asylumCaseNotificationApiSender =
                new AsylumCaseNotificationApiSender(
                        asylumCaseCallbackApiDelegator,
                        ENDPOINT,
                        CCD_SUBMITTED_PATH,
                        true,
                        true,
                        SAVE_NOTIFICATIONS_DATA_SCHEDULE_HOUR,
                        SAVE_NOTIFICATIONS_DATA_SCHEDULE_HOUR,
                        dateProvider,
                        scheduler,
                        featureToggler
                );
        final AsylumCase notifiedAsylumCase = mock(AsylumCase.class);

        when(asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
                .thenReturn(notifiedAsylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(1L);
        LocalDateTime afterScheduleHour = LocalDateTime.now().withHour(SAVE_NOTIFICATIONS_DATA_SCHEDULE_HOUR).withMinute(1);
        when(dateProvider.nowWithTime()).thenReturn(afterScheduleHour);

        when(asylumCase.read(SAVE_NOTIFICATIONS_TO_DATA_DATE, String.class)).thenReturn(Optional.of(LocalDate.now().toString()));

        final AsylumCase callbackResponse = asylumCaseNotificationApiSender.send(callback);

        verifyNoInteractions(scheduler);
    }

    private static void verifyTimedEventSchedule(TimedEvent timedEventCaptorValue) {
        assertEquals(Event.SAVE_NOTIFICATIONS_TO_DATA, timedEventCaptorValue.getEvent());
        assertEquals("IA", timedEventCaptorValue.getJurisdiction());
        assertEquals("Asylum", timedEventCaptorValue.getCaseType());
        assertEquals(1L, timedEventCaptorValue.getCaseId());
        assertEquals(SAVE_NOTIFICATIONS_DATA_SCHEDULE_HOUR, timedEventCaptorValue.getScheduledDateTime().getHour());
    }


    @CsvSource({
        "true, true, false",
        "true, false, false",
        "false, false, false",
        "false, false, true",
        "false, true, true",
        "false, true, false",
    })
    @ParameterizedTest
    void does_not_schedule_save_notification_to_data_event_if_feature_toggle_is_disabled(
            boolean timedEventEnabled,
            boolean saveNotificationFeatureEnabled,
            boolean saveNotificationToDataEnvVarEnabled
    ) {
        asylumCaseNotificationApiSender =
                new AsylumCaseNotificationApiSender(
                        asylumCaseCallbackApiDelegator,
                        ENDPOINT,
                        CCD_SUBMITTED_PATH,
                        timedEventEnabled,
                        saveNotificationToDataEnvVarEnabled,
                        SAVE_NOTIFICATIONS_DATA_SCHEDULE_HOUR,
                        SAVE_NOTIFICATIONS_DATA_SCHEDULE_MAX_MINUTES,
                        dateProvider,
                        scheduler,
                        featureToggler
                );
        final AsylumCase notifiedAsylumCase = mock(AsylumCase.class);

        when(asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
                .thenReturn(notifiedAsylumCase);
        LocalDateTime localDateTime =
                LocalDateTime.of(2004, Month.FEBRUARY, 5, 10, 57, 33);
        when(featureToggler.getValue("save-notifications-feature", false))
                .thenReturn(saveNotificationFeatureEnabled);
        when(dateProvider.nowWithTime()).thenReturn(localDateTime);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(0L);

        final AsylumCase callbackResponse = asylumCaseNotificationApiSender.send(callback);

        verify(scheduler, never()).schedule(any());
        verify(asylumCaseCallbackApiDelegator, times(1))
                .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(notifiedAsylumCase, callbackResponse);
    }

}
