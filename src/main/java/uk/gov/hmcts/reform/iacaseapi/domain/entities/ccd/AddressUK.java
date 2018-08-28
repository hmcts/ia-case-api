package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

public class AddressUK {

    @JsonProperty("AddressLine1")
    private Optional<String> addressLine1 = Optional.empty();

    @JsonProperty("AddressLine2")
    private Optional<String> addressLine2 = Optional.empty();

    @JsonProperty("AddressLine3")
    private Optional<String> addressLine3 = Optional.empty();

    @JsonProperty("PostTown")
    private Optional<String> postTown = Optional.empty();

    @JsonProperty("County")
    private Optional<String> county = Optional.empty();

    @JsonProperty("PostCode")
    private Optional<String> postCode = Optional.empty();

    @JsonProperty("Country")
    private Optional<String> country = Optional.empty();

    private AddressUK() {
        // noop -- for deserializer
    }

    public AddressUK(
        String addressLine1,
        String addressLine2,
        String addressLine3,
        String postTown,
        String county,
        String postCode,
        String country
    ) {
        this.addressLine1 = Optional.ofNullable(addressLine1);
        this.addressLine2 = Optional.ofNullable(addressLine2);
        this.addressLine3 = Optional.ofNullable(addressLine3);
        this.postTown = Optional.ofNullable(postTown);
        this.county = Optional.ofNullable(county);
        this.postCode = Optional.ofNullable(postCode);
        this.country = Optional.ofNullable(country);
    }

    public Optional<String> getAddressLine1() {
        return addressLine1;
    }

    public Optional<String> getAddressLine2() {
        return addressLine2;
    }

    public Optional<String> getAddressLine3() {
        return addressLine3;
    }

    public Optional<String> getPostTown() {
        return postTown;
    }

    public Optional<String> getCounty() {
        return county;
    }

    public Optional<String> getPostCode() {
        return postCode;
    }

    public Optional<String> getCountry() {
        return country;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = Optional.ofNullable(addressLine1);
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = Optional.ofNullable(addressLine2);
    }

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = Optional.ofNullable(addressLine3);
    }

    public void setPostTown(String postTown) {
        this.postTown = Optional.ofNullable(postTown);
    }

    public void setCounty(String county) {
        this.county = Optional.ofNullable(county);
    }

    public void setPostCode(String postCode) {
        this.postCode = Optional.ofNullable(postCode);
    }

    public void setCountry(String country) {
        this.country = Optional.ofNullable(country);
    }
}
