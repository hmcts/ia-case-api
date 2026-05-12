package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        country
    );

    @Test
    public void should_hold_onto_values() {

        Assertions.assertEquals(addressLine1, legRepAddressUk.getAddressLine1());
        Assertions.assertEquals(addressLine2, legRepAddressUk.getAddressLine2());
        Assertions.assertEquals(addressLine3, legRepAddressUk.getAddressLine3());
        Assertions.assertEquals(townCity, legRepAddressUk.getTownCity());
        Assertions.assertEquals(county, legRepAddressUk.getCounty());
        Assertions.assertEquals(postCode, legRepAddressUk.getPostCode());
        Assertions.assertEquals(country, legRepAddressUk.getCountry());
    }
}
