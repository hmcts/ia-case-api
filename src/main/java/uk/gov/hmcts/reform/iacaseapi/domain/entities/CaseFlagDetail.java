package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class CaseFlagDetail {
    private String id;
    @JsonProperty("value")
    private CaseFlagValue caseFlagValue;
}
