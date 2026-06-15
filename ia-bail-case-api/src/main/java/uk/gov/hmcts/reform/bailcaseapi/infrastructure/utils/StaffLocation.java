package uk.gov.hmcts.reform.bailcaseapi.infrastructure.utils;


import uk.gov.hmcts.reform.bailcaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Location;

public class StaffLocation {

    private StaffLocation() {

    }

    public static Location getLocation(HearingCentre hearingCentre) throws IllegalArgumentException {
        return switch (hearingCentre) {
            case BIRMINGHAM -> new Location("", "Birmingham");
            case GLASGOW -> new Location("", "Glasgow");
            case BRADFORD -> new Location("", "Bradford");
            case HATTON_CROSS -> new Location("", "Hatton Cross");
            case MANCHESTER -> new Location("", "Manchester");
            case NEWCASTLE -> new Location("", "Newcastle");
            case NEWPORT -> new Location("", "Newport");
            case TAYLOR_HOUSE -> new Location("", "Taylor House");
            case YARLS_WOOD -> new Location("", "Yarls Wood");
        };
    }
}
