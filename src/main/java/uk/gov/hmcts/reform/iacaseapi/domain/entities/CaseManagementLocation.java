package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CaseManagementLocation {
    Region region;
    BaseLocation baseLocation;
}
