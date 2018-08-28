package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

public class HomeOfficeAsylumData {

    private String number;
    private String date;
    private String outcome;

    @JsonProperty("data_title")
    private Optional<String> title = Optional.empty();

    @JsonProperty("data_first_name")
    private Optional<String> firstName = Optional.empty();

    @JsonProperty("data_last_name")
    private Optional<String> lastName = Optional.empty();

    @JsonProperty("data_date_of_birth")
    private Optional<String> dateOfBirth = Optional.empty();

    @JsonProperty("data_nationality")
    private Optional<String> nationality = Optional.empty();

    @JsonProperty("data_stateless")
    private Optional<String> stateless = Optional.empty();

    @JsonProperty("data_address_1")
    private Optional<String> address1 = Optional.empty();

    @JsonProperty("data_address_2")
    private Optional<String> address2 = Optional.empty();

    @JsonProperty("data_address_town")
    private Optional<String> addressTown = Optional.empty();

    @JsonProperty("data_address_county")
    private Optional<String> addressCounty = Optional.empty();

    @JsonProperty("data_address_postcode")
    private Optional<String> addressPostcode = Optional.empty();

    @JsonProperty("data_address_country")
    private Optional<String> addressCountry = Optional.empty();

    private HomeOfficeAsylumData() {
        // noop -- for deserializer
    }

    public HomeOfficeAsylumData(
        String number,
        String date,
        String outcome,
        Optional<String> title,
        Optional<String> firstName,
        Optional<String> lastName,
        Optional<String> dateOfBirth,
        Optional<String> nationality,
        Optional<String> stateless,
        Optional<String> address1,
        Optional<String> address2,
        Optional<String> addressTown,
        Optional<String> addressCounty,
        Optional<String> addressPostcode,
        Optional<String> addressCountry
    ) {
        this.number = number;
        this.date = date;
        this.outcome = outcome;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        this.stateless = stateless;
        this.address1 = address1;
        this.address2 = address2;
        this.addressTown = addressTown;
        this.addressCounty = addressCounty;
        this.addressPostcode = addressPostcode;
        this.addressCountry = addressCountry;
    }

    public String getNumber() {
        return number;
    }

    public String getDate() {
        return date;
    }

    public String getOutcome() {
        return outcome;
    }

    public Optional<String> getTitle() {
        return title;
    }

    public Optional<String> getFirstName() {
        return firstName;
    }

    public Optional<String> getLastName() {
        return lastName;
    }

    public Optional<String> getDateOfBirth() {
        return dateOfBirth;
    }

    public Optional<String> getNationality() {
        return nationality;
    }

    public Optional<String> getStateless() {
        return stateless;
    }

    public Optional<String> getAddress1() {
        return address1;
    }

    public Optional<String> getAddress2() {
        return address2;
    }

    public Optional<String> getAddressTown() {
        return addressTown;
    }

    public Optional<String> getAddressCounty() {
        return addressCounty;
    }

    public Optional<String> getAddressPostcode() {
        return addressPostcode;
    }

    public Optional<String> getAddressCountry() {
        return addressCountry;
    }
}
