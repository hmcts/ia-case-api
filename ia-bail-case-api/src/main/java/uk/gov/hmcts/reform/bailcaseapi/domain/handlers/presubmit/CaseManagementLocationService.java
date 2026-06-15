package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseManagementLocationRefData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Region;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.LocationRefDataService;

@Service
public class CaseManagementLocationService {

    private final LocationRefDataService locationRefDataService;

    public CaseManagementLocationService(LocationRefDataService locationRefDataService) {
        this.locationRefDataService = locationRefDataService;
    }

    public CaseManagementLocation getCaseManagementLocation(HearingCentre hearingCentre) {
        Optional<BaseLocation> baseLocation = getBaseLocation(hearingCentre);
        return baseLocation.map(location ->
            new CaseManagementLocation(Region.NATIONAL, location))
            .orElseGet(() -> new CaseManagementLocation(Region.NATIONAL, BaseLocation.NEWPORT));
    }

    public CaseManagementLocationRefData getRefDataCaseManagementLocation(HearingCentre hearingCentre) {
        DynamicList cmlRefDataList = locationRefDataService.getCaseManagementLocationDynamicList();

        cmlRefDataList.getListItems().stream()
            .filter(value -> Objects.equals(value.getCode(), hearingCentre.getEpimsId()))
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

    private Optional<BaseLocation> getBaseLocation(HearingCentre hearingCentre) {
        return BaseLocation.fromId(hearingCentre.getEpimsId());
    }
}
