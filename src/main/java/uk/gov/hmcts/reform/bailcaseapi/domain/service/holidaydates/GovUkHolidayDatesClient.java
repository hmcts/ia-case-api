package uk.gov.hmcts.reform.iacaseapi.domain.service.holidaydates;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
    name = "GovUkHolidayDatesClient",
    url = "${govUkHolidays.url}"
)
public interface GovUkHolidayDatesClient {
    @GetMapping(value = "/bank-holidays.json", produces = MediaType.APPLICATION_JSON_VALUE)
    UkHolidayDates getHolidayDates();
}
