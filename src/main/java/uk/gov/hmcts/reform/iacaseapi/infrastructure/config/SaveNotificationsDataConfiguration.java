package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Configuration for the Save Notifications to Data feature.
 *
 * <p>This feature fetches notification details from GOV.UK Notify and stores them
 * in case data. The scheduling configuration controls when this background job runs.
 */
@Getter
@Setter
@Component
@EnableConfigurationProperties
@ConfigurationProperties("saveNotificationsData")
public class SaveNotificationsDataConfiguration {

    private final SecureRandom random = new SecureRandom();

    /**
     * Whether the save notifications feature is enabled.
     */
    private boolean enabled;

    /**
     * Number of days to retain notifications. Only notifications sent within this
     * period will be fetched from GOV.UK Notify.
     */
    private int retentionDays;

    /**
     * Scheduling configuration for the background job that fetches notifications.
     */
    private Scheduling scheduling = new Scheduling();

    /**
     * Calculates a randomized schedule time for the save notifications job.
     *
     * <p>Schedules for today if current time is before {@code targetHour}, otherwise for tomorrow.
     * A random offset in seconds (0 to {@code windowDurationMinutes * 60}) is added to spread load.
     *
     * @param currentTime the current date/time
     * @return the scheduled time with random offset applied
     */
    public ZonedDateTime calculateScheduledTime(ZonedDateTime currentTime) {
        LocalTime targetTime = LocalTime.of(scheduling.getTargetHour(), 0);

        LocalDate scheduleDate = currentTime.toLocalTime().isBefore(targetTime)
            ? currentTime.toLocalDate()
            : currentTime.toLocalDate().plusDays(1);

        int windowSeconds = scheduling.getWindowDurationMinutes() * 60;
        int randomOffsetSeconds = random.nextInt(windowSeconds);

        return scheduleDate
            .atTime(targetTime)
            .plusSeconds(randomOffsetSeconds)
            .atZone(currentTime.getZone());
    }

    /**
     * Calculates a randomized schedule time using the system default timezone.
     *
     * @param currentTime the current local date/time
     * @return the scheduled time with random offset applied
     */
    public ZonedDateTime calculateScheduledTime(java.time.LocalDateTime currentTime) {
        return calculateScheduledTime(ZonedDateTime.of(currentTime, ZoneId.systemDefault()));
    }

    @Getter
    @Setter
    public static class Scheduling {
        private int targetHour;
        private int windowDurationMinutes;
    }
}
