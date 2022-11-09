package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.SubmitEventDetails;

public class SubmitEventDetailsTest {
    private final long id = 1234;
    private final String jurisdiction = "IA";
    private State state;
    private Map<String, Object> data;
    private final int callbackResponseStatusCode = 200;
    private final String callbackResponseStatus = "CALLBACK_COMPLETED";

    private SubmitEventDetails submitEventDetails;

    @Test
    void should_test_equals_contract() {

        EqualsVerifier.simple()
            .forClass(SubmitEventDetails.class)
            .verify();
    }

    @Test
    void should_hold_onto_values() {

        state = State.DECISION_CONDITIONAL_BAIL;
        data = new HashMap<>();
        data.put("legalRepName", "");
        data.put("legalRepPhone", "");

        submitEventDetails =
            new SubmitEventDetails(id, jurisdiction, state, data, callbackResponseStatusCode, callbackResponseStatus);

        assertEquals(id, submitEventDetails.getId());
        assertEquals(jurisdiction, submitEventDetails.getJurisdiction());
        assertEquals(State.DECISION_CONDITIONAL_BAIL, submitEventDetails.getState());
        assertEquals(data, submitEventDetails.getData());
        assertEquals(callbackResponseStatusCode, submitEventDetails.getCallbackResponseStatusCode());
        assertEquals(callbackResponseStatus, submitEventDetails.getCallbackResponseStatus());
    }
}
