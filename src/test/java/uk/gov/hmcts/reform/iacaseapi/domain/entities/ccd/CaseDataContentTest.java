package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CaseDataContentTest {

    @Test
    void should_test_equals_and_hashCode_contract_manually() {
        Map<String, Object> data1 = new HashMap<>();
        data1.put("key", "value");
        Map<String, Object> event1 = new HashMap<>();
        event1.put("id", "event1");

        CaseDataContent obj1 = new CaseDataContent("ref", data1, event1, "token", true);
        CaseDataContent obj2 = new CaseDataContent("ref", data1, event1, "token", true);

        assertEquals(obj1, obj2);
        assertEquals(obj2, obj1);
        assertEquals(obj1, obj2);
        CaseDataContent obj4 = new CaseDataContent("ref", data1, event1, "token", true);
        assertEquals(obj2, obj4);
        assertEquals(obj1, obj4);
        assertEquals(obj1, obj2);
        assertEquals(obj1, obj2);
        assertNotEquals(null, obj1);
        CaseDataContent obj3 = new CaseDataContent("ref2", data1, event1, "token", true);
        assertNotEquals(obj1, obj3);
        assertEquals(obj1.hashCode(), obj2.hashCode());
        assertNotEquals(obj1.hashCode(), obj3.hashCode());
    }

    @Test
    void should_hold_onto_values() {

        Map<String, Object> data = new HashMap<>();
        data.put("paymentStatus", "Success");
        data.put("paymentReference", "RC-1234");

        Map<String, Object> event = new HashMap<>();
        event.put("id", "updatePaymentStatus");

        String caseReference = "1234";
        boolean ignoreWarning = true;
        String eventToken = "eventToken";
        CaseDataContent caseDataContent = new CaseDataContent(caseReference, data, event, eventToken, ignoreWarning);

        assertEquals("1234", caseDataContent.getCaseReference());
        assertEquals(data, caseDataContent.getData());
        assertEquals("updatePaymentStatus", caseDataContent.getEvent().get("id"));
        assertEquals("eventToken", caseDataContent.getEventToken());
        assertTrue(caseDataContent.isIgnoreWarning());
    }
}