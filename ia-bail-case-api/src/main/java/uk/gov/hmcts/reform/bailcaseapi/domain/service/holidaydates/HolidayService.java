package uk.gov.hmcts.reform.bailcaseapi.domain.service.holidaydates;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HolidayService {
    private final List<LocalDate> holidays;

    public HolidayService(List<LocalDate> holidays) {
        this.holidays = holidays;
    }

    public boolean isHoliday(LocalDate localDate) {
        return holidays.contains(localDate);
    }

    public boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
