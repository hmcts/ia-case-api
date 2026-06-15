package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@AllArgsConstructor
public class CaseManagementLocationRefData {
    Region region;
    DynamicList baseLocation;
}
