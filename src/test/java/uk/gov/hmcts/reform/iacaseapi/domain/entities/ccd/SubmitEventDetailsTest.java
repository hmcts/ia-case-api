package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SubmitEventDetailsTest {

    private long id = 1000L;
    private String jurisdiction = "Jurisdiction";
    private State state = State.ENDED;
    private Map<String, Object> data = Map.of("data", "data");
    private int callbackResponseStatusCode = 200;
    private String callbackResponseStatus = "callbackResponseStatus";

    private SubmitEventDetails submitEventDetails = new SubmitEventDetails(
        id,
        jurisdiction,
        state,
        data,
        callbackResponseStatusCode,
        callbackResponseStatus);

    @Test
    void should_hold_onto_values() {
        assertEquals(id, submitEventDetails.getId());
        assertEquals(jurisdiction, submitEventDetails.getJurisdiction());
        assertEquals(state, submitEventDetails.getState());
        assertEquals(data, submitEventDetails.getData());
        assertEquals(callbackResponseStatusCode, submitEventDetails.getCallbackResponseStatusCode());
        assertEquals(callbackResponseStatus, submitEventDetails.getCallbackResponseStatus());
    }
}