package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
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
public class CourtLocationCategory {

    @JsonProperty("service_code")
    private String serviceCode;
    @JsonProperty("court_type_id")
    private String courtTypeId;
    @JsonProperty("court_type")
    private String courtType;
    @JsonProperty("court_venues")
    private List<CourtVenue> courtVenues;
}
