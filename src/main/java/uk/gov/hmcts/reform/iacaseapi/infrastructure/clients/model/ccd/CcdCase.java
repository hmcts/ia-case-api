package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.Map;

/**
 * Represents a single case in the CCD Elasticsearch search results.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CcdCase {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("reference")
    private Long reference;

    @JsonProperty("case_data")
    private Map<String, Object> data;

    public CcdCase() {
        // Default constructor for Jackson
    }

    public CcdCase(Long id, Long reference, Map<String, Object> data) {
        this.id = id;
        this.reference = reference;
        this.data = data;
    }
}

