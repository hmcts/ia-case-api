package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class LegRepAddressUkTest {

    private final String addressLine1 = "A";
    private final String addressLine2 = "B";
    private final String addressLine3 = "C";
    private final String townCity = "D";
    private final String county = "E";
    private final String postCode = "F";
    private final String country = "G";

    private LegRepAddressUk legRepAddressUk = new LegRepAddressUk(
        addressLine1,
        addressLine2,
        addressLine3,
        townCity,
        county,
        postCode,
        country,
        Arrays.asList("A", "B")
    );

    @Test
    public void should_hold_onto_values() {

        Assert.assertEquals(addressLine1, legRepAddressUk.getAddressLine1());
        Assert.assertEquals(addressLine2, legRepAddressUk.getAddressLine2());
        Assert.assertEquals(addressLine3, legRepAddressUk.getAddressLine3());
        Assert.assertEquals(townCity, legRepAddressUk.getTownCity());
        Assert.assertEquals(county, legRepAddressUk.getCounty());
        Assert.assertEquals(postCode, legRepAddressUk.getPostCode());
        Assert.assertEquals(country, legRepAddressUk.getCountry());
    }
}
