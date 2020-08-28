package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;

class CaseDetailsTest {

    final long id = 123L;
    final String jurisdiction = "IA";
    final State state = State.APPEAL_STARTED;
    final CaseData caseData = mock(CaseData.class);
    final LocalDateTime createdDate = LocalDateTime.parse("2019-01-31T11:22:33");

    CaseDetails<CaseData> caseDetails = new CaseDetails<>(
        id,
        jurisdiction,
        state,
        caseData,
        createdDate
    );

    @Test
    void should_hold_onto_values() {

        assertEquals(id, caseDetails.getId());
        assertEquals(jurisdiction, caseDetails.getJurisdiction());
        assertEquals(state, caseDetails.getState());
        assertEquals(caseData, caseDetails.getCaseData());
        assertEquals(createdDate, caseDetails.getCreatedDate());
    }

    @Test
    void should_throw_required_field_missing_exception() {

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            id,
            null,
            null,
            null,
            null
        );

        assertThatThrownBy(caseDetails::getJurisdiction)
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("jurisdiction");

        assertThatThrownBy(caseDetails::getCaseData)
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("caseData");

    }
}
