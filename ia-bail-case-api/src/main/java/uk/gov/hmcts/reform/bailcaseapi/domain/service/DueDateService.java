package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import java.time.LocalDate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.holidaydates.HolidayService;

@Component
public class DueDateService {

    private final HolidayService holidayService;

    public DueDateService(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    public LocalDate calculateHearingDirectionDueDate(LocalDate delayUntil, LocalDate currentDate) {
        return minusWorkingDays(delayUntil, 2, currentDate);
    }

    private LocalDate minusWorkingDays(LocalDate dueDate, int numberOfDays, LocalDate currentDate) {
        if (dueDate.isEqual(currentDate)
            || dueDate.isBefore(currentDate)) {
            return addWorkingDays(currentDate, 1);
        }

        if (numberOfDays == 0) {
            return dueDate;
        }

        LocalDate newDate = dueDate.minusDays(1);
        if (holidayService.isWeekend(newDate) || holidayService.isHoliday(newDate)) {
            return minusWorkingDays(newDate, numberOfDays, currentDate);
        } else {
            return minusWorkingDays(newDate, numberOfDays - 1, currentDate);
        }
    }

    private LocalDate addWorkingDays(LocalDate dueDate, int numberOfDays) {
        if (numberOfDays == 0) {
            return dueDate;
        }

        LocalDate newDate = dueDate.plusDays(1);
        if (holidayService.isWeekend(newDate) || holidayService.isHoliday(newDate)) {
            return addWorkingDays(newDate, numberOfDays);
        } else {
            return addWorkingDays(newDate, numberOfDays - 1);
        }
    }
}
