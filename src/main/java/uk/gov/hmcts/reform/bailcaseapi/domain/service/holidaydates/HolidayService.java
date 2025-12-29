package uk.gov.hmcts.reform.bailcaseapi.domain.service.holidaydates;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HolidayService {
    private final List<LocalDate> holidays;

    public HolidayService(List<LocalDate> holidays) {
        this.holidays = holidays;
    }

    public boolean isHoliday(ZonedDateTime zonedDateTime) {
        return holidays.contains(zonedDateTime.toLocalDate());
    }

    public boolean isWeekend(ZonedDateTime date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
