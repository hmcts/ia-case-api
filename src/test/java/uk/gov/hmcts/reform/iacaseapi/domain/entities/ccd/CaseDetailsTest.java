package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.exceptions.RequiredFieldMissingException;

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

    @Test
    public void should_throw_required_field_missing_exception() {

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            id,
            null,
            null,
            null
        );

        assertThatThrownBy(caseDetails::getJurisdiction)
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("jurisdiction");
        assertThatThrownBy(caseDetails::getState)
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("state");
        assertThatThrownBy(caseDetails::getCaseData)
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("caseData");

    }

}
