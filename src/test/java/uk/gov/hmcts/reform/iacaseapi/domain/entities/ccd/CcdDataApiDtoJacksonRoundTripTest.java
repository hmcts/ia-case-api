package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;

class CcdDataApiDtoJacksonRoundTripTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    void caseDataContent_serializes_snake_case_and_round_trips() throws Exception {
        CaseDataContent content = new CaseDataContent(
            "1234",
            Map.of("appealType", "protection"),
            Map.of("id", "updatePaymentStatus"),
            "event-token",
            true
        );

        String json = mapper.writeValueAsString(content);

        assertThat(json).contains("\"case_reference\":\"1234\"");
        assertThat(json).contains("\"event_token\":\"event-token\"");
        assertThat(json).contains("\"ignore_warning\":true");
        assertThat(json).doesNotContain("caseReference");
        assertThat(json).doesNotContain("eventToken");
        assertThat(json).doesNotContain("ignoreWarning");

        CaseDataContent roundTrip = mapper.readValue(json, CaseDataContent.class);
        assertEquals("1234", roundTrip.getCaseReference());
        assertEquals("event-token", roundTrip.getEventToken());
        assertTrue(roundTrip.isIgnoreWarning());
    }

    @Test
    void startEventDetails_deserializes_snake_case_ccd_payload() throws Exception {
        String json = """
            {
              "event_id": "reTriggerWaTasks",
              "token": "event-token",
              "case_details": {
                "id": 1234,
                "jurisdiction": "IA",
                "state": "appealSubmitted",
                "case_data": {},
                "created_date": "2019-01-31T11:22:33",
                "security_classification": "PUBLIC",
                "supplementary_data": { "HMCTSServiceId": "BFA1" }
              }
            }
            """;

        StartEventDetails details = mapper.readValue(json, StartEventDetails.class);

        assertEquals(Event.RE_TRIGGER_WA_TASKS, details.getEventId());
        assertEquals("event-token", details.getToken());
        assertEquals(1234L, details.getCaseDetails().getId());
        assertEquals("IA", details.getCaseDetails().getJurisdiction());
        assertEquals(State.APPEAL_SUBMITTED, details.getCaseDetails().getState());
        assertEquals("PUBLIC", details.getCaseDetails().getSecurityClassification());
        assertEquals(LocalDateTime.parse("2019-01-31T11:22:33"), details.getCaseDetails().getCreatedDate());
        assertThat(details.getCaseDetails().getCaseData()).isInstanceOf(AsylumCase.class);
    }

    @Test
    void submitEventDetails_deserializes_snake_case_ccd_payload() throws Exception {
        String json = """
            {
              "id": 1234,
              "jurisdiction": "IA",
              "state": "appealSubmitted",
              "data": { "legalRepName": "" },
              "callback_response_status_code": 200,
              "callback_response_status": "CALLBACK_COMPLETED"
            }
            """;

        SubmitEventDetails details = mapper.readValue(json, SubmitEventDetails.class);

        assertEquals(1234L, details.getId());
        assertEquals(200, details.getCallbackResponseStatusCode());
        assertEquals("CALLBACK_COMPLETED", details.getCallbackResponseStatus());
        assertEquals(State.APPEAL_SUBMITTED, details.getState());
    }

    @Test
    void caseDetails_serializes_snake_case_and_does_not_emit_camel_case() throws Exception {
        JsonNode serviceId = mapper.readTree("\"BFA1\"");
        CaseDetails<AsylumCase> caseDetails = new CaseDetails<>(
            123L,
            "IA",
            State.APPEAL_STARTED,
            new AsylumCase(),
            LocalDateTime.parse("2019-01-31T11:22:33"),
            "PUBLIC",
            Map.of("HMCTSServiceId", serviceId)
        );

        String json = mapper.writeValueAsString(caseDetails);

        assertThat(json).contains("\"case_data\"");
        assertThat(json).contains("\"created_date\"");
        assertThat(json).contains("\"security_classification\"");
        assertThat(json).contains("\"supplementary_data\"");
        assertThat(json).doesNotContain("caseData");
        assertThat(json).doesNotContain("createdDate");
        assertThat(json).doesNotContain("securityClassification");
        assertThat(json).doesNotContain("supplementaryData");
    }
}
