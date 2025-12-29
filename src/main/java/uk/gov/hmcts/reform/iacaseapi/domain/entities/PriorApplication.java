package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class PriorApplication {

    private String applicationId;
    private String caseDataJson;

    private PriorApplication() {
        // noop -- for deserializer
    }

    public PriorApplication(String applicationId, String caseDataJson) {
        this.applicationId = applicationId;
        this.caseDataJson = caseDataJson;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getCaseDataJson() {
        return caseDataJson;
    }
}
