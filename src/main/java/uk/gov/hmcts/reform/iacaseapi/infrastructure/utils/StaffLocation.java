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
                return new Location("", "Birmingham");
            case "glasgow":
            case "glasgowTribunalsCentre":
            case "belfast":
                return new Location("", "Glasgow");
            case "bradford":
                return new Location("", "Bradford");
            case "hattonCross":
                return new Location("", "Hatton Cross");
            case "manchester":
                return new Location("", "Manchester");
            case "newcastle":
                return new Location("", "Newcastle");
            case "newport":
                return new Location("", "Newport");
            case "taylorHouse":
                return new Location("", "Taylor House");
            case "northShields":
                return new Location("", "North Shields");
            case "harmondsworth":
                return new Location("", "Harmondsworth");
            case "yarlswood":
                return new Location("", "Yarls Wood");
            case "remoteHearing":
                return new Location("", "Remote hearing");
            case "decisionWithoutHearing":
                return new Location("", "Decision Without Hearing");
            default:
                throw new IllegalArgumentException("no hearing centre found");
        }
    }
}
