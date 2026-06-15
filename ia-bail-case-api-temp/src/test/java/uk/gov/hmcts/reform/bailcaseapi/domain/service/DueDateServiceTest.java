package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.holidaydates.HolidayService;

@ExtendWith(MockitoExtension.class)
class DueDateServiceTest {

    @Mock
    HolidayService holidayService;

    private DueDateService dueDateService;

    @BeforeEach
    void setUp() {
        dueDateService = new DueDateService(holidayService);
    }

    @Test
    void should_return_next_working_day_when_calculated_due_date_matches_holiday() {
        LocalDate eventDate = LocalDate.of(2023, 12, 25);

        when(holidayService.isWeekend(eventDate.minusDays(1)))
            .thenReturn(true);
        when(holidayService.isWeekend(eventDate.minusDays(2)))
            .thenReturn(true);

        LocalDate expectedDueDate = eventDate.minusDays(3);
        LocalDate actualDateTime = dueDateService.calculateHearingDirectionDueDate(eventDate, LocalDate.of(2023, 12, 21));

        assertEquals(expectedDueDate, actualDateTime);
        verify(holidayService, times(5)).isWeekend(any(LocalDate.class));
        verify(holidayService, times(3)).isHoliday(any(LocalDate.class));
    }

    @Test
    void should_return_2_working_days_before_date() {
        LocalDate eventDate = LocalDate.of(2023, 12, 25);

        when(holidayService.isWeekend(eventDate.minusDays(1)))
            .thenReturn(false);
        when(holidayService.isWeekend(eventDate.minusDays(2)))
            .thenReturn(false);

        LocalDate expectedDueDate = eventDate.minusDays(2);
        LocalDate actualDateTime = dueDateService.calculateHearingDirectionDueDate(eventDate, LocalDate.of(2023, 12, 21));

        assertEquals(expectedDueDate, actualDateTime);
        verify(holidayService, times(2)).isWeekend(any(LocalDate.class));
        verify(holidayService, times(2)).isHoliday(any(LocalDate.class));
    }

    @Test
    void should_return_next_working_day_when_calculated_due_date_matches_current_date() {
        LocalDate eventAndCurrentDate = LocalDate.of(2023, 12, 25);

        when(holidayService.isHoliday(eventAndCurrentDate.plusDays(1)))
            .thenReturn(true);

        LocalDate expectedDueDate = eventAndCurrentDate.plusDays(2);
        LocalDate actualDateTime = dueDateService.calculateHearingDirectionDueDate(eventAndCurrentDate, eventAndCurrentDate);

        assertEquals(actualDateTime, expectedDueDate);
        verify(holidayService, times(2)).isHoliday(any(LocalDate.class));
    }
}

