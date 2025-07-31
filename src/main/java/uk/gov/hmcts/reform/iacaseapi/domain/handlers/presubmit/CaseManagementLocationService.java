package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocationRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Region;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

@Service
public class CaseManagementLocationService {

    private final LocationRefDataService locationRefDataService;

    public CaseManagementLocationService(LocationRefDataService locationRefDataService) {
        this.locationRefDataService = locationRefDataService;
    }

    public CaseManagementLocation getCaseManagementLocation(String staffLocationName) {
        Optional<BaseLocation> baseLocation = getBaseLocation(staffLocationName);
        return baseLocation.map(location ->
            new CaseManagementLocation(Region.NATIONAL, location))
            .orElseGet(() -> new CaseManagementLocation(Region.NATIONAL, BaseLocation.NEWPORT));
    }

    public CaseManagementLocationRefData getRefDataCaseManagementLocation(String staffLocationName) {
        Optional<BaseLocation> baseLocation = getBaseLocation(staffLocationName);
        DynamicList cmlRefDataList = locationRefDataService.getCaseManagementLocationDynamicList();

        cmlRefDataList.getListItems().stream()
            .filter(value -> Objects.equals(value.getCode(), baseLocation.get().getId()))
            .findFirst().ifPresentOrElse(
                cmlRefDataList::setValue,
                () -> cmlRefDataList.setValue(getDefaultCourtValue(cmlRefDataList)));

        return new CaseManagementLocationRefData(Region.NATIONAL, cmlRefDataList);
    }

    private static Value getDefaultCourtValue(DynamicList refDataCourtList) {
        return refDataCourtList.getListItems().stream()
            .filter(v -> Objects.equals(v.getCode(), BaseLocation.NEWPORT.getId()))
            .findFirst().orElseThrow(() -> new IllegalStateException("Newport location doesn't exist in ref data"));
    }

    private Optional<BaseLocation> getBaseLocation(String staffLocationName) {
        String fromStaffLocationNameToBaseLocationEnumName =
                StringUtils.upperCase(staffLocationName)
                        .replace(" ", "_")
                        .replace("(", "")
                        .replace(")", "");
        BaseLocation baseLocation = BaseLocation.valueOf(fromStaffLocationNameToBaseLocationEnumName);
        return Optional.of(baseLocation);

    }
}
