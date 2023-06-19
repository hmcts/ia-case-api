package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class CaseManagementLocation {
    Region region;
    BaseLocation baseLocation;
}
