package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;

@ExtendWith(MockitoExtension.class)
class StartEventDetailsTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Test
    void should_hold_onto_values() {
        Event eventId = Event.RE_TRIGGER_WA_TASKS;

        long caseId = 1234;
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);
        String jurisdiction = "IA";
        when(caseDetails.getJurisdiction()).thenReturn(jurisdiction);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        String token = "eventToken";
        StartEventDetails startEventDetails = new StartEventDetails(eventId, token, caseDetails);

        assertEquals(Event.RE_TRIGGER_WA_TASKS, startEventDetails.getEventId());
        assertEquals(token, startEventDetails.getToken());
        assertEquals(caseId, startEventDetails.getCaseDetails().getId());
        assertEquals(jurisdiction, startEventDetails.getCaseDetails().getJurisdiction());
        assertEquals(State.APPEAL_SUBMITTED, startEventDetails.getCaseDetails().getState());
        assertEquals(asylumCase, startEventDetails.getCaseDetails().getCaseData());
    }
}