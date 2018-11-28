package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class AddressUkTest {

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
    public void should_hold_onto_values() {

        Assert.assertEquals(Optional.of(addressLine1), addressUk.getAddressLine1());
        Assert.assertEquals(Optional.of(addressLine2), addressUk.getAddressLine2());
        Assert.assertEquals(Optional.of(addressLine3), addressUk.getAddressLine3());
        Assert.assertEquals(Optional.of(postTown), addressUk.getPostTown());
        Assert.assertEquals(Optional.of(county), addressUk.getCounty());
        Assert.assertEquals(Optional.of(postCode), addressUk.getPostCode());
        Assert.assertEquals(Optional.of(country), addressUk.getCountry());
    }
}
