package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.Value;

@Value
public class CaseManagementLocation {
    Region region;
    BaseLocation baseLocation;
}
