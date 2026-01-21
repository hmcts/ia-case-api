package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static java.util.Objects.requireNonNull;

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
    private Optional<String> postcode = Optional.empty();

    @JsonProperty("Country")
    private Optional<String> country = Optional.empty();

    private AddressUK() {

    }

    public AddressUK(
        String addressLine1,
        String addressLine2,
        String addressLine3,
        String postTown,
        String county,
        String postcode,
        String country
    ) {
        this.addressLine1 = Optional.ofNullable(addressLine1);
        this.addressLine2 = Optional.ofNullable(addressLine2);
        this.addressLine3 = Optional.ofNullable(addressLine3);
        this.postTown = Optional.ofNullable(postTown);
        this.county = Optional.ofNullable(county);
        this.postcode = Optional.ofNullable(postcode);
        this.country = Optional.ofNullable(country);
    }

    public Optional<String> getAddressLine1() {
        requireNonNull(addressLine1);
        return addressLine1;
    }

    public Optional<String> getAddressLine2() {
        requireNonNull(addressLine2);
        return addressLine2;
    }

    public Optional<String> getAddressLine3() {
        requireNonNull(addressLine3);
        return addressLine3;
    }

    public Optional<String> getPostTown() {
        requireNonNull(postTown);
        return postTown;
    }

    public Optional<String> getCounty() {
        requireNonNull(county);
        return county;
    }

    public Optional<String> getPostCode() {
        requireNonNull(postcode);
        return postcode;
    }

    public Optional<String> getCountry() {
        requireNonNull(country);
        return country;
    }
}
