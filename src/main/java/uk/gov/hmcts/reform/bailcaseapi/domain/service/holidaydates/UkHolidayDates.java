package uk.gov.hmcts.reform.iacaseapi.domain.service.holidaydates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class UkHolidayDates {
    @JsonProperty("england-and-wales")
    private CountryHolidayDates englandAndWales;

    private UkHolidayDates() {
    }

    public UkHolidayDates(CountryHolidayDates englandAndWales) {
        this.englandAndWales = englandAndWales;
    }

    public CountryHolidayDates getEnglandAndWales() {
        return englandAndWales;
    }

}
