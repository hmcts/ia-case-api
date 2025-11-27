package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Represents an Elasticsearch query for CCD case search.
 */
@Data
public class CcdSearchQuery {

    @JsonProperty("query")
    private Map<String, Object> query;

    @JsonProperty("size")
    private int size;

    @JsonProperty("_source")
    private List<String> source;

    public CcdSearchQuery(Map<String, Object> query, int size, List<String> source) {
        this.query = query;
        this.size = size;
        this.source = source;
    }
}
