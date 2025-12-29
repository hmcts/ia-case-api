package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LegRepAddressUk {

    private String addressLine1;

    private String addressLine2;

    private String addressLine3;

    private String townCity;

    private String county;

    private String postCode;

    private String country;

    private LegRepAddressUk() {
        // noop -- for deserializer
    }

    public LegRepAddressUk(
        String addressLine1,
        String addressLine2,
        String addressLine3,
        String townCity,
        String county,
        String postCode,
        String country
    ) {
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressLine3 = addressLine3;
        this.townCity = townCity;
        this.county = county;
        this.postCode = postCode;
        this.country = country;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public String getTownCity() {
        return townCity;
    }

    public String getCounty() {
        return county;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getCountry() {
        return country;
    }

}
