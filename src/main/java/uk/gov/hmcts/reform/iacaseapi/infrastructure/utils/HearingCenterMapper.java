package uk.gov.hmcts.reform.iacaseapi.infrastructure.utils;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.casemanagementlocation.BaseLocation;

public class HearingCenterMapper {

    private HearingCenterMapper() {

    }

    public static BaseLocation getBaseLocation(HearingCentre hearingCentre) throws IllegalArgumentException {
        String centre = hearingCentre.getValue();
        switch (centre) {
            case "birmingham":
            case "nottingham":
            case "coventry":
                return BaseLocation.BIRMINGHAM;
            case "glasgow":
            case "glasgowTribunalsCentre":
            case "belfast":
                return BaseLocation.GLASGOW;
            case "bradford":
                return BaseLocation.BRADFORD;
            case "hattonCross":
                return BaseLocation.HATTON_CROSS;
            case "manchester":
                return BaseLocation.MANCHESTER;
            case "newcastle":
                return BaseLocation.NEWCASTLE;
            case "newport":
                return BaseLocation.NEWPORT;
            case "taylorHouse":
                return BaseLocation.TAYLOR_HOUSE;
            default:
                throw new IllegalArgumentException("no hearing centre found");
        }
    }
}
