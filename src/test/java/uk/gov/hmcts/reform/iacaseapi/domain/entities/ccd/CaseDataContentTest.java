package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class CaseDataContentTest {

    private final String caseReference = "caseReference";
    private final Map<String, Object> data = Map.of("data", "data");
    private final Map<String, Object> event = Map.of("data", "data");
    private final String eventToken = "eventToken";
    private final boolean ignoreWarning = true;

    private CaseDataContent caseDataContent = new CaseDataContent(
        caseReference,
        data,
        event,
        eventToken,
        ignoreWarning);

    @Test
    void should_hold_onto_values() {
        assertEquals(caseReference, caseDataContent.getCaseReference());
        assertEquals(data, caseDataContent.getData());
        assertEquals(event, caseDataContent.getEvent());
        assertEquals(eventToken, caseDataContent.getEventToken());
        assertEquals(ignoreWarning, caseDataContent.isIgnoreWarning());
    }
}
