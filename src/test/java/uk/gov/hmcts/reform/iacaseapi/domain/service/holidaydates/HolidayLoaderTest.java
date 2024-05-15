package uk.gov.hmcts.reform.iacaseapi.domain.service.holidaydates;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
class HolidayLoaderTest {
    @Test
    void loadData() {
        GovUkHolidayDatesClient govUkHolidayDatesClient = mock(GovUkHolidayDatesClient.class);
        LocalDate holiday = LocalDate.now();
        when(govUkHolidayDatesClient.getHolidayDates()).thenReturn(
            new UkHolidayDates(new CountryHolidayDates(singletonList(new HolidayDate(holiday))))
        );
        List<LocalDate> holidays = new HolidayLoader(govUkHolidayDatesClient).loadHolidays();

        assertThat(holidays, is(singletonList(holiday)));
    }
}
