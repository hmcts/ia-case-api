package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.service.holidaydates.HolidayService;

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
    void should_return_next_working_day_4_pm_when_calculated_due_date_matches_holiday() {
        ZonedDateTime eventDateTime =
            ZonedDateTime.of(
                2022, 7, 19,
                9, 0, 0, 0,
                ZoneId.systemDefault()
            );

        int workingDaysAllowed = 2;

        when(holidayService.isHoliday(eventDateTime.plusDays(1)))
            .thenReturn(true);

        ZonedDateTime expectedDueDate = eventDateTime.plusDays(workingDaysAllowed + 1);
        ZonedDateTime expectedDueDateTime = expectedDueDate.with(
            LocalTime.of(16, 0, 0, 0)
        );
        ZonedDateTime actualDateTime = dueDateService.calculateDueDate(eventDateTime, workingDaysAllowed);

        assertThat(actualDateTime, is(expectedDueDateTime));
        verify(holidayService, times(3)).isHoliday(any(ZonedDateTime.class));
    }

    @Test
    void should_return_next_working_day_4_pm_when_delay_date_matches_holiday() {
        ZonedDateTime eventDateTime =
            ZonedDateTime.of(
                2022, 7, 19,
                9, 0, 0, 0,
                ZoneId.systemDefault()
            );

        int delayDuration = 2;

        when(holidayService.isHoliday(eventDateTime.plusDays(delayDuration)))
            .thenReturn(true);

        ZonedDateTime expectedDelayDate = eventDateTime.plusDays(delayDuration + 1);
        ZonedDateTime expectedDelayDateTime = expectedDelayDate.with(LocalTime.of(16, 0, 0, 0));
        ZonedDateTime actualDateTime = dueDateService.calculateDelayUntil(eventDateTime, delayDuration);

        assertThat(actualDateTime, is(expectedDelayDateTime));
        verify(holidayService, times(2)).isHoliday(any(ZonedDateTime.class));
    }


}

