package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Jacksonized
@AllArgsConstructor
public class CourtVenue {

    @JsonProperty("site_name")
    private String siteName;
    @JsonProperty("court_name")
    private String courtName;
    @JsonProperty("epimms_id")
    private String epimmsId;
    @JsonProperty("is_hearing_location")
    private String isHearingLocation;
    @JsonProperty("is_case_management_location")
    private String isCaseManagementLocation;
    @JsonProperty("court_status")
    private String courtStatus;
    @JsonProperty("court_address")
    private String courtAddress;
    private String postcode;
    @JsonProperty("location_type")
    private String locationType;
}
