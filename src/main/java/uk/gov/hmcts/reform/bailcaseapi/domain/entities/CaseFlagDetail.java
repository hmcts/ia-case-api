package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseFlagDetail {

    private CaseFlagDetail() {
        // noop -- for deserializer
    }

    private String id;
    @JsonProperty("value")
    private CaseFlagValue caseFlagValue;
}
