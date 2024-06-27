package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;

@SuppressWarnings("unchecked")
class StartEventDetailsTest {

    private Event eventId = Event.SUBMIT_APPEAL;
    private String token = "token";
    private CaseDetails<AsylumCase> caseDetails = mock(CaseDetails.class);

    private StartEventDetails startEventDetails = new StartEventDetails(eventId, token, caseDetails);

    @Test
    void should_hold_onto_values() {
        assertEquals(eventId, startEventDetails.getEventId());
        assertEquals(token, startEventDetails.getToken());
        assertEquals(caseDetails, startEventDetails.getCaseDetails());
    }
}
