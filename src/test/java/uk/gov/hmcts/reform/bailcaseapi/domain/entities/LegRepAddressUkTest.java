package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        country
    );

    @Test
    public void should_hold_onto_values() {

        assertEquals(addressLine1, legRepAddressUk.getAddressLine1());
        assertEquals(addressLine2, legRepAddressUk.getAddressLine2());
        assertEquals(addressLine3, legRepAddressUk.getAddressLine3());
        assertEquals(townCity, legRepAddressUk.getTownCity());
        assertEquals(county, legRepAddressUk.getCounty());
        assertEquals(postCode, legRepAddressUk.getPostCode());
        assertEquals(country, legRepAddressUk.getCountry());

    }
}
