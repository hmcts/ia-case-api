package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.UUID;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseFlagDetail {

    String id;
    @JsonProperty("value")
    CaseFlagValue caseFlagValue;

    public CaseFlagDetail(CaseFlagValue caseFlagValue) {
        this(UUID.randomUUID().toString(), caseFlagValue);
    }

    public CaseFlagDetail(String id, CaseFlagValue caseFlagValue) {
        this.id = id;
        this.caseFlagValue = caseFlagValue;
    }
}