package uk.gov.hmcts.reform.iacaseapi;


import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacaseapi.util.AuthorizationHeadersProvider;
import uk.gov.hmcts.reform.iacaseapi.util.MapSerializer;

@SpringBootTest
@ActiveProfiles("functional")
@DirtiesContext
@SuppressWarnings("Unchecked")
public class DataFixingTest {

    @Value("${targetInstance}")
    private String targetInstance;

    private final List<String> callbackEndpoints =
        Arrays.asList(
            "/asylum/ccdAboutToStart",
            "/asylum/ccdAboutToSubmit"
        );

    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void corrects_field_names() throws Exception {

        Map<String, Object> asylumCaseMap = new HashMap<>();
        asylumCaseMap.put("appealType", "refusalOfHumanRights");
        asylumCaseMap.put("respondentsAgreedScheduleOfIssuesDescription", "some-value");

        String callbackBody = buildCallbackBodyWithMinimalRequired(
            asylumCaseMap,
            "appealSubmitted",
            "startAppeal"
        );

        callbackEndpoints.forEach(endpoint -> {

            String responseJson =
                SerenityRest
                    .given()
                    .headers(getAuthorizationHeaders())
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                    .body(callbackBody)
                    .when()
                    .post(endpoint)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .and()
                    .extract().body().asString();

            Map<String, String> caseDetails = extractCaseData(responseJson);

            assertThat(caseDetails.get("respondentsAgreedScheduleOfIssuesDescription")).isNull();
            assertThat(caseDetails.get("appellantsAgreedScheduleOfIssuesDescription")).isEqualTo("some-value");
        });
    }

    private Map<String, String> extractCaseData(String responseJson) {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Map<String, String>> responseMap;

        try {
            responseMap = objectMapper.readValue(responseJson, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read response", e);
        }

        return responseMap.get("data");
    }

    private Headers getAuthorizationHeaders() {

        return authorizationHeadersProvider
            .getLegalRepresentativeOrgSuccessAuthorization();

    }

    private String buildCallbackBodyWithMinimalRequired(
        Map<String, Object> caseData,
        String state,
        String eventId
    ) throws IOException {

        caseData.put("appellantGivenNames", "some-given-name");
        caseData.put("appellantFamilyName", "some-family-name");
        caseData.put("homeOfficeReferenceNumber", "some-ref-number");

        LocalDateTime createdDate = LocalDateTime.now();

        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("id", "1");
        caseDetails.put("jurisdiction", "IA");
        caseDetails.put("state", state);
        caseDetails.put("created_date", createdDate.toString());
        caseDetails.put("case_data", caseData);

        Map<String, Object> callback = new HashMap<>();
        callback.put("event_id", eventId);
        callback.put("case_details", caseDetails);

        return MapSerializer.serialize(callback);
    }

}
