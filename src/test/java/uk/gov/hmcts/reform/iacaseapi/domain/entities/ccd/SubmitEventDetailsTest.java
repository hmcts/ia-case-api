package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SubmitEventDetailsTest {

    @Test
    void should_test_equals_and_hashCode_contract_manually() {
        Map<String, Object> data1 = new HashMap<>();
        data1.put("legalRepName", "");
        data1.put("legalRepPhone", "");

        Map<String, Object> data2 = new HashMap<>();
        data2.put("legalRepName", "");
        data2.put("legalRepPhone", "");

        SubmitEventDetails obj1 = new SubmitEventDetails(1234, "IA", State.APPEAL_SUBMITTED, data1, 200, "CALLBACK_COMPLETED");
        SubmitEventDetails obj2 = new SubmitEventDetails(1234, "IA", State.APPEAL_SUBMITTED, data2, 200, "CALLBACK_COMPLETED");

        assertEquals(obj1, obj2);
        assertEquals(obj2, obj1);
        assertEquals(obj1, obj2);
        SubmitEventDetails obj4 = new SubmitEventDetails(1234, "IA", State.APPEAL_SUBMITTED, data1, 200, "CALLBACK_COMPLETED");
        assertEquals(obj2, obj4);
        assertEquals(obj1, obj4);
        assertEquals(obj1, obj2);
        assertEquals(obj1, obj2);
        assertNotEquals(null, obj1);
        SubmitEventDetails obj3 = new SubmitEventDetails(5678, "IA", State.APPEAL_SUBMITTED, data1, 200, "CALLBACK_COMPLETED");
        assertNotEquals(obj1, obj3);
        assertEquals(obj1.hashCode(), obj2.hashCode());
        assertNotEquals(obj1.hashCode(), obj3.hashCode());
    }

    @Test
    void should_hold_onto_values() {

        State state = State.APPEAL_SUBMITTED;
        Map<String, Object> data = new HashMap<>();
        data.put("legalRepName", "");
        data.put("legalRepPhone", "");

        long id = 1234;
        String jurisdiction = "IA";
        int callbackResponseStatusCode = 200;
        String callbackResponseStatus = "CALLBACK_COMPLETED";
        SubmitEventDetails submitEventDetails = new SubmitEventDetails(id, jurisdiction, state, data, callbackResponseStatusCode, callbackResponseStatus);

        assertEquals(id, submitEventDetails.getId());
        assertEquals(jurisdiction, submitEventDetails.getJurisdiction());
        assertEquals(State.APPEAL_SUBMITTED, submitEventDetails.getState());
        assertEquals(data, submitEventDetails.getData());
        assertEquals(callbackResponseStatusCode, submitEventDetails.getCallbackResponseStatusCode());
        assertEquals(callbackResponseStatus, submitEventDetails.getCallbackResponseStatus());
    }
}