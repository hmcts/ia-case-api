package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;

@ExtendWith(MockitoExtension.class)
class StartEventDetailsTest {

    private Event eventId;
    private String token = "eventToken";
    private long caseId = 1234;
    private String jurisdiction = "IA";

    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;

    private StartEventDetails startEventDetails;

    @Test
    void should_hold_onto_values() {

        eventId = Event.CLEAR_LEGAL_REPRESENTATIVE_DETAILS;

        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getState()).thenReturn(State.DECISION_DECIDED);
        when(caseDetails.getJurisdiction()).thenReturn(jurisdiction);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

        startEventDetails = new StartEventDetails(eventId, token, caseDetails);

        assertEquals(Event.CLEAR_LEGAL_REPRESENTATIVE_DETAILS, startEventDetails.getEventId());
        assertEquals(token, startEventDetails.getToken());
        assertEquals(caseId, startEventDetails.getCaseDetails().getId());
        assertEquals(jurisdiction, startEventDetails.getCaseDetails().getJurisdiction());
        assertEquals(State.DECISION_DECIDED, startEventDetails.getCaseDetails().getState());
        assertEquals(bailCase, startEventDetails.getCaseDetails().getCaseData());
    }
}
