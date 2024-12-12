package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TtlCcdObject {

    @JsonProperty("Suspended")
    private String suspended;

    @JsonProperty("SystemTTL")
    private String systemTTL;

    @JsonProperty("OverrideTTL")
    private String overrideTTL;
}
