package uk.gov.hmcts.reform.iacaseapi.infrastructure.utils;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Location;

public class StaffLocation {

    private StaffLocation() {

    }

    public static Location getLocation(HearingCentre hearingCentre) throws IllegalArgumentException {
        String centre = hearingCentre.getValue();
        switch (centre) {
            case "birmingham":
            case "nottingham":
            case "coventry":
                return new Location("231596", "Birmingham");
            case "glasgow":
            case "glasgowTribunalsCentre":
            case "belfast":
                return new Location("198444", "Glasgow");
            case "bradford":
                return new Location("698118", "Bradford");
            case "hattonCross":
                return new Location("386417", "Hatton Cross");
            case "manchester":
                return new Location("512401", "Manchester");
            case "newcastle":
                return new Location("", "Newcastle");
            case "newport":
                return new Location("227101", "Newport");
            case "taylorHouse":
                return new Location("765324", "Taylor House");
            default:
                throw new IllegalArgumentException("no hearing centre found");
        }
    }
}
