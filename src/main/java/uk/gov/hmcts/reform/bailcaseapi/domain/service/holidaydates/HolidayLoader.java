package uk.gov.hmcts.reform.bailcaseapi.domain.service.holidaydates;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class HolidayLoader {
    private final GovUkHolidayDatesClient govUkHolidayDatesClient;

    public HolidayLoader(GovUkHolidayDatesClient govUkHolidayDatesClient) {
        this.govUkHolidayDatesClient = govUkHolidayDatesClient;
    }

    @Bean
    public List<LocalDate> loadHolidays() {
        UkHolidayDates holidayDates = govUkHolidayDatesClient.getHolidayDates();
        return holidayDates.getEnglandAndWales().getEvents().stream()
            .map(HolidayDate::getDate)
            .collect(Collectors.toList());
    }
}
