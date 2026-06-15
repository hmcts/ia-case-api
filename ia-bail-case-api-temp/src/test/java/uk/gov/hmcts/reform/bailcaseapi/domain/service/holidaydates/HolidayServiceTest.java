package uk.gov.hmcts.reform.bailcaseapi.domain.service.holidaydates;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;
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
        HOLIDAYS.forEach(localDate -> assertTrue(holidayService.isHoliday(localDate)));
    }

    @Test
    void testIsHolidayZoneDateTimeReturnsFalse() {
        LocalDate zonedDateTime =
            LocalDate.of(2022, Month.SEPTEMBER, 8);
        assertFalse(holidayService.isHoliday(zonedDateTime));
    }

    @Test
    void testIsWeekendZonedDateTimeReturnsTrue() {
        LocalDate saturdaySeptemberTenth =
            LocalDate.of(2022, Month.SEPTEMBER, 10);

        LocalDate sundaySeptemberEleventh =
            LocalDate.of(2022, Month.SEPTEMBER, 11);

        assertTrue(holidayService.isWeekend(saturdaySeptemberTenth));
        assertTrue(holidayService.isWeekend(sundaySeptemberEleventh));
    }

    @Test
    void testIsWeekendZoneDateTimeReturnsFalse() {
        LocalDate fridaySeptemberNinth =
            LocalDate.of(2022, Month.SEPTEMBER, 9);

        assertFalse(holidayService.isWeekend(fridaySeptemberNinth));
    }
}
