package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class CaseDetailsTest {

    private final long id = 123L;
    private final String jurisdiction = "IA";
    private final State state = State.APPEAL_STARTED;
    private final CaseData caseData = mock(CaseData.class);

    private CaseDetails<CaseData> caseDetails = new CaseDetails<>(
        id,
        jurisdiction,
        state,
        caseData
    );

    @Test
    public void should_hold_onto_values() {

        assertEquals(id, caseDetails.getId());
        assertEquals(jurisdiction, caseDetails.getJurisdiction());
        assertEquals(state, caseDetails.getState());
        assertEquals(caseData, caseDetails.getCaseData());
    }
}
