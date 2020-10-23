package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Region;

@Service
public class CaseManagementLocationService {

    private static final String LOCATION_WITH_NO_CODE = "Newcastle";

    public CaseManagementLocation getCaseManagementLocation(String staffLocationName) {
        Optional<BaseLocation> baseLocation = getBaseLocation(staffLocationName);
        return baseLocation.map(location ->
            new CaseManagementLocation(Region.NATIONAL, location))
            .orElseGet(() -> new CaseManagementLocation(Region.NATIONAL, null));
    }

    private Optional<BaseLocation> getBaseLocation(String staffLocationName) {
        if (!LOCATION_WITH_NO_CODE.equals(staffLocationName)) {
            String fromStaffLocationNameToBaseLocationEnumName =
                StringUtils.upperCase(staffLocationName).replace(" ", "_");
            BaseLocation baseLocation = BaseLocation.valueOf(fromStaffLocationNameToBaseLocationEnumName);
            return Optional.of(baseLocation);
        }
        return Optional.empty();
    }
}