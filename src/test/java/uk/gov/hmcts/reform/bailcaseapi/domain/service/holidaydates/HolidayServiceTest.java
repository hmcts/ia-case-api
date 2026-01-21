package uk.gov.hmcts.reform.iacaseapi.domain.service.holidaydates;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HolidayServiceTest {

    private static final List<LocalDate> HOLIDAYS = List.of(
        LocalDate.of(2022, Month.DECEMBER, 25),
        LocalDate.of(2022, Month.JANUARY, 1),
        LocalDate.of(2022, Month.APRIL, 15)
    );
    private HolidayService holidayService;

    @BeforeEach
    void setup() {
        holidayService = new HolidayService(HOLIDAYS);
    }

    @Test
    void testIsHolidayZoneDateTimeReturnsTrue() {
        HOLIDAYS.stream()
            .map(localDate -> ZonedDateTime.of(localDate.atTime(11, 30), ZoneId.systemDefault()))
            .forEach(zonedDateTime -> assertTrue(holidayService.isHoliday(zonedDateTime)));
    }

    @Test
    void testIsHolidayZoneDateTimeReturnsFalse() {
        ZonedDateTime zonedDateTime =
            ZonedDateTime.of(
                LocalDateTime.of(2022, Month.SEPTEMBER, 8, 13, 16), ZoneId.systemDefault());
        assertFalse(holidayService.isHoliday(zonedDateTime));
    }

    @Test
    void testIsWeekendZonedDateTimeReturnsTrue() {
        ZonedDateTime saturdaySeptemberTenth =
            ZonedDateTime.of(
                LocalDateTime.of(2022, Month.SEPTEMBER, 10, 13, 16), ZoneId.systemDefault());

        ZonedDateTime sundaySeptemberEleventh =
            ZonedDateTime.of(
                LocalDateTime.of(2022, Month.SEPTEMBER, 11, 13, 16), ZoneId.systemDefault());

        assertTrue(holidayService.isWeekend(saturdaySeptemberTenth));
        assertTrue(holidayService.isWeekend(sundaySeptemberEleventh));
    }

    @Test
    void testIsWeekendZoneDateTimeReturnsFalse() {
        ZonedDateTime fridaySeptemberNinth =
            ZonedDateTime.of(
                LocalDateTime.of(2022, Month.SEPTEMBER, 9, 13, 16), ZoneId.systemDefault());

        assertFalse(holidayService.isWeekend(fridaySeptemberNinth));
    }
}
