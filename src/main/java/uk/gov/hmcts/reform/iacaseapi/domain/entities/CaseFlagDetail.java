package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.UUID;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseFlagDetail {

    String id;
    CaseFlagValue value;

    public CaseFlagDetail(CaseFlagValue value) {
        this(UUID.randomUUID().toString(), value);
    }

    public CaseFlagDetail(@JsonProperty("id") String id, @JsonProperty("value") CaseFlagValue value) {
        this.id = id;
        this.value = value;
    }
}