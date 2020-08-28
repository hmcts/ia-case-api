package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class AddressUkTest {

    final String addressLine1 = "A";
    final String addressLine2 = "B";
    final String addressLine3 = "C";
    final String postTown = "D";
    final String county = "E";
    final String postCode = "F";
    final String country = "G";

    AddressUk addressUk = new AddressUk(
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
}
