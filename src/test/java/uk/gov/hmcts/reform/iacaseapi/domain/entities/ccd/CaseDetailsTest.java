package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;

class CaseDetailsTest {

    private final long id = 123L;
    private final String jurisdiction = "IA";
    private final State state = State.APPEAL_STARTED;
    private final CaseData caseData = mock(CaseData.class);
    private final LocalDateTime createdDate = LocalDateTime.parse("2019-01-31T11:22:33");
    private final String classification = "PUBLIC";

    static ObjectMapper mapper = new ObjectMapper();
    static JsonNode serviceIdValue;

    static {
        try {
            serviceIdValue = mapper.readValue("\"BFA1\"", JsonNode.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private static final Map<String, JsonNode> supplementaryData = new HashMap<>();

    static {
        supplementaryData.put("HMCTSServiceId:", serviceIdValue);
    }


    private final CaseDetails<CaseData> caseDetails = new CaseDetails<>(
        id,
        jurisdiction,
        state,
        caseData,
        createdDate,
        classification,
        supplementaryData
    );

    @Test
    void should_hold_onto_values() {

        assertEquals(id, caseDetails.getId());
        assertEquals(jurisdiction, caseDetails.getJurisdiction());
        assertEquals(state, caseDetails.getState());
        assertEquals(caseData, caseDetails.getCaseData());
        assertEquals(createdDate, caseDetails.getCreatedDate());
        assertEquals(classification, caseDetails.getSecurityClassification());
        assertEquals(supplementaryData, caseDetails.getSupplementaryData());
    }

    @Test
    void should_throw_required_field_missing_exception() throws JsonProcessingException {

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            id,
            null,
            null,
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
