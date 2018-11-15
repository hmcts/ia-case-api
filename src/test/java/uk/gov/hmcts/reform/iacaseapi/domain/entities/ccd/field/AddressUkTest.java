package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class AddressUkTest {

    private final Optional<String> addressLine1 = Optional.of("A");
    private final Optional<String> addressLine2 = Optional.of("B");
    private final Optional<String> addressLine3 = Optional.of("C");
    private final Optional<String> postTown = Optional.of("D");
    private final Optional<String> county = Optional.of("E");
    private final Optional<String> postCode = Optional.of("F");
    private final Optional<String> country = Optional.of("G");

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

        Assert.assertEquals(addressLine1, addressUk.getAddressLine1());
        Assert.assertEquals(addressLine2, addressUk.getAddressLine2());
        Assert.assertEquals(addressLine3, addressUk.getAddressLine3());
        Assert.assertEquals(postTown, addressUk.getPostTown());
        Assert.assertEquals(county, addressUk.getCounty());
        Assert.assertEquals(postCode, addressUk.getPostCode());
        Assert.assertEquals(country, addressUk.getCountry());
    }
}
