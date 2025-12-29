package uk.gov.hmcts.reform.iacaseapi.domain.service.holidaydates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@ToString
public class HolidayDate {
    private LocalDate date;

    private HolidayDate() {
    }

    public HolidayDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

}
