package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Value;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrategicCaseFlag {

    String partyName;
    String roleOnCase;

    @JsonProperty("details")
    List<CaseFlagDetail> details;
}
