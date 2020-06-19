package uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class WorkAllocationRequest {
    private Map<String, WorkAllocationVariable> variables;

    public WorkAllocationRequest(Map<String, WorkAllocationVariable> variables) {
        this.variables = variables;
    }

    public Map<String, WorkAllocationVariable> getVariables() {
        return variables;
    }
}
