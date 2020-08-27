package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import java.util.Optional;
import org.junit.Assert;
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

        Assert.assertEquals(Optional.of(addressLine1), addressUk.getAddressLine1());
        Assert.assertEquals(Optional.of(addressLine2), addressUk.getAddressLine2());
        Assert.assertEquals(Optional.of(addressLine3), addressUk.getAddressLine3());
        Assert.assertEquals(Optional.of(postTown), addressUk.getPostTown());
        Assert.assertEquals(Optional.of(county), addressUk.getCounty());
        Assert.assertEquals(Optional.of(postCode), addressUk.getPostCode());
        Assert.assertEquals(Optional.of(country), addressUk.getCountry());
    }
}
