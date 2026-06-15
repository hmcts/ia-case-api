package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.NOTIFICATION_STORE_SCHEDULE_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.SAVE_NOTIFICATIONS_TO_DATA_BAIL;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.NotificationSender;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.TimedEvent;

@Slf4j
@Service
public class BailCaseNotificationApiSender implements NotificationSender<BailCase> {

    private final BailCaseCallbackApiDelegator bailCaseCallbackApiDelegator;
    private final String notificationsApiEndpoint;
    private final String aboutToSubmitPath;
    private final boolean timedEventServiceEnabled;
    private final int saveNotificationScheduleAtHour;
    private final int saveNotificationScheduleMaxMinutes;
    private final DateProvider dateProvider;
    private final Scheduler scheduler;
    SecureRandom random = new SecureRandom();

    public BailCaseNotificationApiSender(
        BailCaseCallbackApiDelegator bailCaseCallbackApiDelegator,
        @Value("${notificationsApi.endpoint}") String notificationsApiEndpoint,
        @Value("${notificationsApi.aboutToSubmitPath}") String aboutToSubmitPath,
        @Value("${featureFlag.timedEventServiceEnabled}") boolean timedEventServiceEnabled,
        @Value("${saveNotificationsDataJob.scheduleAtHour}") int saveNotificationScheduleAtHour,
        @Value("${saveNotificationsDataJob.scheduleMaxMinutes}") int saveNotificationScheduleMaxMinutes,
        DateProvider dateProvider,
        Scheduler scheduler
    ) {
        this.bailCaseCallbackApiDelegator = bailCaseCallbackApiDelegator;
        this.notificationsApiEndpoint = notificationsApiEndpoint;
        this.aboutToSubmitPath = aboutToSubmitPath;
        this.timedEventServiceEnabled = timedEventServiceEnabled;
        this.saveNotificationScheduleAtHour = saveNotificationScheduleAtHour;
        this.saveNotificationScheduleMaxMinutes = saveNotificationScheduleMaxMinutes;
        this.dateProvider = dateProvider;
        this.scheduler = scheduler;
    }

    public BailCase send(
        Callback<BailCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");
        BailCase bailCase = callback.getCaseDetails().getCaseData();
        LocalDate notificationScheduleDate = bailCase.read(NOTIFICATION_STORE_SCHEDULE_DATE, String.class)
            .map(LocalDate::parse)
            .orElse(LocalDate.of(1999, 10, 2));

        if (timedEventServiceEnabled && !notificationScheduleDate.isEqual(LocalDate.now())) {
            scheduleSaveNotificationToData(callback);
        } else {
            log.info("Skipping saveNotificationsToDate event schedule");
        }


        return bailCaseCallbackApiDelegator.delegate(
            callback,
            notificationsApiEndpoint + aboutToSubmitPath
        );
    }

    private void scheduleSaveNotificationToData(Callback<BailCase> callback) {
        BailCase bailCase = callback.getCaseDetails().getCaseData();
        try {
            scheduler.schedule(
                new TimedEvent(
                    "",
                    SAVE_NOTIFICATIONS_TO_DATA_BAIL,
                    determineScheduleTime(),
                    "IA",
                    "Bail",
                    callback.getCaseDetails().getId()
                )
            );
            bailCase.write(NOTIFICATION_STORE_SCHEDULE_DATE, LocalDate.now().toString());
        } catch (BailCaseServiceResponseException e) {
            log.error("Scheduling SAVE_NOTIFICATIONS_TO_DATA event failed: ", e);
        }
    }

    private ZonedDateTime determineScheduleTime() {
        ZonedDateTime now = ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault());
        // Define saveNotificationScheduleAtHour eg:11:00 PM as the base time
        LocalTime baseTime = LocalTime.of(saveNotificationScheduleAtHour, 0);

        // Randomize minutes and seconds between 0-saveNotificationScheduleMaxMinutes minutes and 0-59 seconds
        int randomMinutes = random.nextInt(0, saveNotificationScheduleMaxMinutes);
        int randomSeconds = random.nextInt(0, 60);

        // If notification sent time is before saveNotificationScheduleAtHour
        if (now.toLocalTime().isBefore(baseTime)) {
            return now.toLocalDate().atTime(baseTime).plusMinutes(randomMinutes).plusSeconds(randomSeconds)
                .atZone(now.getZone());
        } else {
            // If notification sent time is after saveNotificationScheduleAtHour eg: 11 PM, schedule for the next day
            return now.toLocalDate().plusDays(1).atTime(baseTime).plusMinutes(randomMinutes).plusSeconds(randomSeconds)
                .atZone(now.getZone());
        }
    }

}
