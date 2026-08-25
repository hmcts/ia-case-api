package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SaveNotificationsDataConfigurationTest {

    private SaveNotificationsDataConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new SaveNotificationsDataConfiguration();
        configuration.getScheduling().setTargetHour(23);
        configuration.getScheduling().setWindowDurationMinutes(50);
    }

    @Test
    void should_schedule_for_today_when_current_time_is_before_target_hour() {
        ZonedDateTime currentTime = ZonedDateTime.of(2024, 1, 15, 10, 0, 0, 0, ZoneId.of("Europe/London"));

        ZonedDateTime scheduledTime = configuration.calculateScheduledTime(currentTime);

        assertEquals(currentTime.toLocalDate(), scheduledTime.toLocalDate());
        assertEquals(23, scheduledTime.getHour());
        assertTrue(scheduledTime.getMinute() < 50);
    }

    @Test
    void should_schedule_for_next_day_when_current_time_is_after_target_hour() {
        ZonedDateTime currentTime = ZonedDateTime.of(2024, 1, 15, 23, 30, 0, 0, ZoneId.of("Europe/London"));

        ZonedDateTime scheduledTime = configuration.calculateScheduledTime(currentTime);

        assertEquals(currentTime.toLocalDate().plusDays(1), scheduledTime.toLocalDate());
        assertEquals(23, scheduledTime.getHour());
        assertTrue(scheduledTime.getMinute() < 50);
    }

    @Test
    void should_schedule_for_next_day_when_current_time_equals_target_hour() {
        ZonedDateTime currentTime = ZonedDateTime.of(2024, 1, 15, 23, 0, 0, 0, ZoneId.of("Europe/London"));

        ZonedDateTime scheduledTime = configuration.calculateScheduledTime(currentTime);

        assertEquals(currentTime.toLocalDate().plusDays(1), scheduledTime.toLocalDate());
        assertEquals(23, scheduledTime.getHour());
    }

    @Test
    void should_schedule_within_window_duration() {
        ZonedDateTime currentTime = ZonedDateTime.of(2024, 1, 15, 10, 0, 0, 0, ZoneId.of("Europe/London"));

        for (int i = 0; i < 100; i++) {
            ZonedDateTime scheduledTime = configuration.calculateScheduledTime(currentTime);

            assertEquals(23, scheduledTime.getHour());
            assertTrue(scheduledTime.getMinute() >= 0 && scheduledTime.getMinute() < 50,
                "Minutes should be within window: " + scheduledTime.getMinute());
            assertTrue(scheduledTime.getSecond() >= 0 && scheduledTime.getSecond() < 60,
                "Seconds should be valid: " + scheduledTime.getSecond());
        }
    }

    @Test
    void should_use_custom_target_hour_and_window() {
        configuration.getScheduling().setTargetHour(14);
        configuration.getScheduling().setWindowDurationMinutes(30);

        ZonedDateTime currentTime = ZonedDateTime.of(2024, 1, 15, 10, 0, 0, 0, ZoneId.of("Europe/London"));

        ZonedDateTime scheduledTime = configuration.calculateScheduledTime(currentTime);

        assertEquals(currentTime.toLocalDate(), scheduledTime.toLocalDate());
        assertEquals(14, scheduledTime.getHour());
        assertTrue(scheduledTime.getMinute() < 30);
    }

    @Test
    void should_handle_local_date_time_overload() {
        LocalDateTime currentTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0);

        ZonedDateTime scheduledTime = configuration.calculateScheduledTime(currentTime);

        assertEquals(currentTime.toLocalDate(), scheduledTime.toLocalDate());
        assertEquals(23, scheduledTime.getHour());
        assertTrue(scheduledTime.getMinute() < 50);
    }

}
