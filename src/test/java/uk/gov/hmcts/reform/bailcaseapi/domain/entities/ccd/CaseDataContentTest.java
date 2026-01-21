package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class CaseDataContentTest {
    private Map<String, Object> data;
    private Map<String, Object> event;
    private final String caseReference = "1234";
    private final String eventToken = "eventToken";
    private final boolean ignoreWarning = true;
    private CaseDataContent caseDataContent;

    @Test
    void should_test_equals_contract() {

        EqualsVerifier.simple()
            .forClass(CaseDataContent.class)
            .verify();
    }

    @Test
    void should_hold_onto_values() {

        data = new HashMap<>();
        data.put("paymentStatus", "Success");
        data.put("paymentReference", "RC-1234");

        event = new HashMap<>();
        event.put("id", "updatePaymentStatus");

        caseDataContent =
            new CaseDataContent(caseReference, data, event, eventToken, ignoreWarning);

        assertEquals("1234", caseDataContent.getCaseReference());
        assertEquals(data, caseDataContent.getData());
        assertEquals("updatePaymentStatus", caseDataContent.getEvent().get("id"));
        assertEquals("eventToken", caseDataContent.getEventToken());
        assertEquals(true, caseDataContent.isIgnoreWarning());
    }
}
