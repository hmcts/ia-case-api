package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.refdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Jacksonized
@AllArgsConstructor
public class CourtVenue {

    private String siteName;
    private String courtName;
    private String epimmsId;
    private String courtStatus;
    private String isHearingLocation;
    private String isCaseManagementLocation;
    private String courtAddress;
    private String postcode;
}
