package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Represents a single case in the CCD Elasticsearch search results.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CcdCase {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("reference")
    private Long reference;

    @JsonProperty("data")
    private Map<String, Object> data;

    public CcdCase() {
        // Default constructor for Jackson
    }

    public CcdCase(Long id, Long reference, Map<String, Object> data) {
        this.id = id;
        this.reference = reference;
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReference() {
        return reference;
    }

    public void setReference(Long reference) {
        this.reference = reference;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}

