package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.NOTIFICATION_STORE_SCHEDULE_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.SAVE_NOTIFICATIONS_TO_DATA_BAIL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.TimedEvent;

@ExtendWith(MockitoExtension.class)
class BailCaseNotificationApiSenderTest {

    private static final String ENDPOINT = "http://endpoint";
    private static final String CCD_SUBMITTED_PATH = "/path";

    @Mock
    private BailCaseCallbackApiDelegator bailCaseCallbackApiDelegator;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private Scheduler scheduler;
    @Captor
    private ArgumentCaptor<TimedEvent> timedEventCaptor;

    private BailCaseNotificationApiSender bailCaseNotificationApiSender;

    @Test
    void should_delegate_callback_to_downstream_api() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(bailCase);
        bailCaseNotificationApiSender =
            new BailCaseNotificationApiSender(
                bailCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH,
                false,
                23,
                59,
                dateProvider,
                scheduler
            );
        final BailCase bailCaseResponse = bailCaseNotificationApiSender.send(callback);

        verify(bailCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(bailCase, bailCaseResponse);
    }

    @Test
    void should_delegate_about_to_start_callback_to_downstream_api() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(bailCase);

        bailCaseNotificationApiSender =
            new BailCaseNotificationApiSender(
                bailCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH,
                false,
                23,
                59,
                dateProvider,
                scheduler
            );
        final BailCase actualBailCase = bailCaseNotificationApiSender.send(callback);

        verify(bailCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(bailCase, actualBailCase);
    }

    @Test
    void should_not_allow_null_arguments() {
        bailCaseNotificationApiSender =
            new BailCaseNotificationApiSender(
                bailCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH,
                false,
                23,
                59,
                dateProvider,
                scheduler
            );
        assertThatThrownBy(() -> bailCaseNotificationApiSender.send(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_skip_save_notification_to_data_schedule_when_tes_not_enabled() {
        bailCaseNotificationApiSender =
            new BailCaseNotificationApiSender(
                bailCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH,
                false,
                23,
                59,
                dateProvider,
                scheduler
            );
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(bailCase);

        final BailCase bailCaseResponse = bailCaseNotificationApiSender.send(callback);

        verify(bailCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(bailCase, bailCaseResponse);
        verify(scheduler, never()).schedule(any(TimedEvent.class));
    }

    @Test
    void should_skip_save_notification_to_data_schedule_when_already_scheduled() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(NOTIFICATION_STORE_SCHEDULE_DATE, String.class))
            .thenReturn(Optional.of(LocalDate.now().toString()));
        when(bailCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(bailCase);

        bailCaseNotificationApiSender =
            new BailCaseNotificationApiSender(
                bailCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH,
                true,
                23,
                59,
                dateProvider,
                scheduler
            );
        final BailCase bailCaseResponse = bailCaseNotificationApiSender.send(callback);

        verify(bailCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(bailCase, bailCaseResponse);
        verify(scheduler, never()).schedule(any(TimedEvent.class));
    }

    @Test
    void should_schedule_save_notification_to_data_today_if_before_time() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(NOTIFICATION_STORE_SCHEDULE_DATE, String.class))
            .thenReturn(Optional.of(LocalDate.now().minusDays(1).toString()));
        when(bailCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(bailCase);
        LocalDateTime today = LocalDateTime.of(2024, 6, 1, 10, 0);
        when(dateProvider.nowWithTime()).thenReturn(today);

        bailCaseNotificationApiSender =
            new BailCaseNotificationApiSender(
                bailCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH,
                true,
                23,
                59,
                dateProvider,
                scheduler
            );
        final BailCase bailCaseResponse = bailCaseNotificationApiSender.send(callback);

        verify(bailCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(bailCase, bailCaseResponse);

        verify(scheduler, times(1)).schedule(timedEventCaptor.capture());
        ZonedDateTime zonedDateTime = LocalDateTime.of(2024, 6, 1, 23, 0)
            .atZone(ZoneId.systemDefault());
        verifyTimedEventSchedule(callback.getCaseDetails().getId(), timedEventCaptor.getValue(), zonedDateTime);
        verify(bailCase, times(1))
            .write(NOTIFICATION_STORE_SCHEDULE_DATE, LocalDate.now().toString());
    }

    @Test
    void should_schedule_save_notification_to_data_tomorrow_if_after_time() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(NOTIFICATION_STORE_SCHEDULE_DATE, String.class))
            .thenReturn(Optional.of(LocalDate.now().minusDays(1).toString()));
        when(bailCaseCallbackApiDelegator.delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(bailCase);
        LocalDateTime today = LocalDateTime.of(2024, 6, 1, 23, 5);
        when(dateProvider.nowWithTime()).thenReturn(today);

        bailCaseNotificationApiSender =
            new BailCaseNotificationApiSender(
                bailCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH,
                true,
                23,
                59,
                dateProvider,
                scheduler
            );
        final BailCase bailCaseResponse = bailCaseNotificationApiSender.send(callback);

        verify(bailCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(bailCase, bailCaseResponse);

        verify(scheduler, times(1)).schedule(timedEventCaptor.capture());
        ZonedDateTime zonedDateTime = LocalDateTime.of(2024, 6, 2, 23, 0)
            .atZone(ZoneId.systemDefault());
        verifyTimedEventSchedule(callback.getCaseDetails().getId(), timedEventCaptor.getValue(), zonedDateTime);
        verify(bailCase, times(1))
            .write(NOTIFICATION_STORE_SCHEDULE_DATE, LocalDate.now().toString());
    }

    private static void verifyTimedEventSchedule(long caseId, TimedEvent timedEventCaptorValue, ZonedDateTime scheduledFor) {
        assertEquals(SAVE_NOTIFICATIONS_TO_DATA_BAIL, timedEventCaptorValue.getEvent());
        assertEquals("IA", timedEventCaptorValue.getJurisdiction());
        assertEquals("Bail", timedEventCaptorValue.getCaseType());
        assertEquals(caseId, timedEventCaptorValue.getCaseId());
        assertEquals(scheduledFor.getDayOfMonth(), timedEventCaptorValue.getScheduledDateTime().getDayOfMonth());
        assertEquals(scheduledFor.getMonth(), timedEventCaptorValue.getScheduledDateTime().getMonth());
        assertEquals(scheduledFor.getMonth(), timedEventCaptorValue.getScheduledDateTime().getMonth());
        assertEquals(scheduledFor.getHour(), timedEventCaptorValue.getScheduledDateTime().getHour());
    }
}
