package uk.gov.hmcts.reform.bailcaseapi.domain.service.holidaydates;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UkHolidayDatesTest {

    private final List<HolidayDate> events = List.of(new HolidayDate(LocalDate.now()));
    private final CountryHolidayDates countryHolidayDates = new CountryHolidayDates(events);
    private final UkHolidayDates ukHolidayDates = new UkHolidayDates(countryHolidayDates);

    @Test
    void should_hold_unto_value() {
        assertEquals(countryHolidayDates, ukHolidayDates.getEnglandAndWales());

    }
}
