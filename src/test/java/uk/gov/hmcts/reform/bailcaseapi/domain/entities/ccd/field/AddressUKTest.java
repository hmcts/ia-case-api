package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class AddressUKTest {

    private final String addressLine1 = "A";
    private final String addressLine2 = "B";
    private final String addressLine3 = "C";
    private final String postTown = "D";
    private final String county = "E";
    private final String postCode = "F";
    private final String country = "G";

    private AddressUK addressUk = new AddressUK(
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
