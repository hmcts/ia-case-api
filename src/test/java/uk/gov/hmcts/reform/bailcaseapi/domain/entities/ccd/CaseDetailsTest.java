package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;

public class CaseDetailsTest {

    private final long id = 123L;
    private final String jurisdiction = "IA";
    private final State state = State.APPLICATION_STARTED;
    private final CaseData caseData = mock(CaseData.class);
    private final LocalDateTime createdDate = LocalDateTime.parse("2022-01-05T11:00:22");
    private final String classification = "PUBLIC";

    private CaseDetails<CaseData> caseDetails = new CaseDetails<>(
        id,
        jurisdiction,
        state,
        caseData,
        createdDate,
        classification
    );

    @Test
    void should_hold_values() {

        assertEquals(id, caseDetails.getId());
        assertEquals(jurisdiction, caseDetails.getJurisdiction());
        assertEquals(state, caseDetails.getState());
        assertEquals(caseData, caseDetails.getCaseData());
        assertEquals(createdDate, caseDetails.getCreatedDate());
        assertEquals(classification, caseDetails.getSecurityClassification());
    }

    @Test
    void should_throw_missing_field_exception() {

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            id,
            null,
            null,
            null,
            createdDate,
            classification
        );

        assertThatThrownBy(caseDetails::getJurisdiction)
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("jurisdiction");

        assertThatThrownBy(caseDetails::getCaseData)
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("caseData");
    }

}
