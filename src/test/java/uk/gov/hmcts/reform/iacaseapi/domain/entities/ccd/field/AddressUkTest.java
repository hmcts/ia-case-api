package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class AddressUkTest {

    private final String addressLine1 = "A";
    private final String addressLine2 = "B";
    private final String addressLine3 = "C";
    private final String postTown = "D";
    private final String county = "E";
    private final String postCode = "F";
    private final String country = "G";

    private AddressUk addressUk = new AddressUk(
        addressLine1,
        addressLine2,
        addressLine3,
        postTown,
        county,
        postCode,
        country
    );

    @Test
    void should_hold_onto_values() {

        assertEquals(Optional.of(addressLine1), addressUk.getAddressLine1());
        assertEquals(Optional.of(addressLine2), addressUk.getAddressLine2());
        assertEquals(Optional.of(addressLine3), addressUk.getAddressLine3());
        assertEquals(Optional.of(postTown), addressUk.getPostTown());
        assertEquals(Optional.of(county), addressUk.getCounty());
        assertEquals(Optional.of(postCode), addressUk.getPostCode());
        assertEquals(Optional.of(country), addressUk.getCountry());
    }

    @Test
    void toDisplay_returns_expected_value() {
        assertEquals("A\r\nB\r\nC\r\nD\r\nE\r\nF\r\nG", addressUk.toDisplay());
    }


    @Test
    void toDisplay_ignores_null_and_blanks() {
        addressUk = new AddressUk(
            addressLine1,
            null,
            "",
            " ",
            county,
            postCode,
            country
        );
        assertEquals("A\r\nE\r\nF\r\nG", addressUk.toDisplay());
    }
}
