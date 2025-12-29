package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriorApplicationTest {

    private final String applicationId = "someAppId1";
    private final String caseDataJson = "{\"exampleField\" : \"exampleData\"";

    private PriorApplication priorApplication = new PriorApplication(
        applicationId,
        caseDataJson
    );

    @Test
    public void should_hold_onto_values() {

        assertEquals(applicationId, priorApplication.getApplicationId());
        assertEquals(caseDataJson, priorApplication.getCaseDataJson());

    }
}
