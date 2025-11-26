package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Represents an Elasticsearch query for CCD case search.
 */
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

    public Map<String, Object> getQuery() {
        return query;
    }

    public void setQuery(Map<String, Object> query) {
        this.query = query;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<String> getSource() {
        return source;
    }

    public void setSource(List<String> source) {
        this.source = source;
    }
}

