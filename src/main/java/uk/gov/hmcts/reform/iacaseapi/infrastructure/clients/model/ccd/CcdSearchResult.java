package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents the response from an Elasticsearch query to CCD.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CcdSearchResult {

    @JsonProperty("total")
    private int total;

    @JsonProperty("cases")
    private List<CcdCase> cases;

    public CcdSearchResult() {
        // Default constructor for Jackson
    }

    public CcdSearchResult(int total, List<CcdCase> cases) {
        this.total = total;
        this.cases = cases;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<CcdCase> getCases() {
        return cases;
    }

    public void setCases(List<CcdCase> cases) {
        this.cases = cases;
    }
}

