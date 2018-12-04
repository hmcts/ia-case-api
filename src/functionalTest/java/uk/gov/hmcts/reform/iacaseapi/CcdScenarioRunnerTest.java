package uk.gov.hmcts.reform.iacaseapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.util.*;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
public class CcdScenarioRunnerTest {

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:8090"
        );

    @Autowired private AuthorizationHeadersProvider authorizationHeadersProvider;
    @Autowired private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void scenarios_should_behave_as_specified() throws IOException {

        String scenarioPattern = System.getProperty("scenario");
        if (scenarioPattern == null) {
            scenarioPattern = "*.json";
        } else {
            scenarioPattern = "*" + scenarioPattern + "*.json";
        }

        Collection<String> scenarioSources =
            StringResourceLoader
                .load("/scenarios/" + scenarioPattern)
                .values();

        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");
        System.out.println((char) 27 + "[33m" + "RUNNING " + scenarioSources.size() + " SCENARIOS");
        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");

        for (String scenarioSource : scenarioSources) {

            Map<String, Object> scenario = deserializeWithExpandedValues(scenarioSource);

            String description = MapValueExtractor.extract(scenario, "description");
            if (MapValueExtractor.extractOrDefault(scenario, "disabled", false)) {
                System.out.println((char) 27 + "[31m" + "SCENARIO: " + description + " **disabled**");
                continue;
            }

            System.out.println((char) 27 + "[33m" + "SCENARIO: " + description);

            Map<String, String> templatesByFilename = StringResourceLoader.load("/templates/*.json");

            final Headers authorizationHeaders = getAuthorizationHeaders(scenario);
            final String requestBody = buildCallbackBody(
                MapValueExtractor.extract(scenario, "request"),
                templatesByFilename
            );

            final String requestUri = MapValueExtractor.extract(scenario, "request.uri");
            final int expectedStatus = MapValueExtractor.extractOrDefault(scenario, "expectation.status", 200);

            String actualResponseBody =
                SerenityRest
                    .given()
                    .headers(authorizationHeaders)
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                    .body(requestBody)
                    .when()
                    .post(requestUri)
                    .then()
                    .statusCode(expectedStatus)
                    .and()
                    .extract()
                    .body()
                    .asString();

            String expectedResponseBody = buildCallbackResponseBody(
                MapValueExtractor.extract(scenario, "expectation"),
                templatesByFilename
            );

            Map<String, Object> actualResponse = MapSerializer.deserialize(actualResponseBody);
            Map<String, Object> expectedResponse = MapSerializer.deserialize(expectedResponseBody);

            MapFieldAssertor.assertFields(expectedResponse, actualResponse, (description + ": "));
        }
    }

    private Map<String, Object> deserializeWithExpandedValues(
        String source
    ) throws IOException {
        Map<String, Object> data = MapSerializer.deserialize(source);
        MapValueExpander.expandValues(data);
        return data;
    }

    private Map<String, Object> buildCaseData(
        Map<String, Object> caseDataInput,
        Map<String, String> templatesByFilename
    ) throws IOException {

        String templateFilename = MapValueExtractor.extract(caseDataInput, "template");

        Map<String, Object> caseData = deserializeWithExpandedValues(templatesByFilename.get(templateFilename));
        Map<String, Object> caseDataReplacements = MapValueExtractor.extract(caseDataInput, "replacements");
        if (caseDataReplacements != null) {
            MapMerger.merge(caseData, caseDataReplacements);
        }

        return caseData;
    }

    private String buildCallbackBody(
        Map<String, Object> data,
        Map<String, String> templatesByFilename
    ) throws IOException {

        Map<String, Object> caseData = buildCaseData(
            MapValueExtractor.extract(data, "input.caseData"),
            templatesByFilename
        );

        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("id", MapValueExtractor.extractOrDefault(data, "input.id", 1));
        caseDetails.put("jurisdiction", MapValueExtractor.extractOrDefault(data, "input.jurisdiction", "IA"));
        caseDetails.put("state", MapValueExtractor.extractOrThrow(data, "input.state"));
        caseDetails.put("case_data", caseData);

        Map<String, Object> callback = new HashMap<>();
        callback.put("event_id", MapValueExtractor.extractOrThrow(data, "input.eventId"));
        callback.put("case_details", caseDetails);

        return MapSerializer.serialize(callback);
    }

    private String buildCallbackResponseBody(
        Map<String, Object> scenario,
        Map<String, String> templatesByFilename
    ) throws IOException {

        if (MapValueExtractor.extract(scenario, "confirmation") != null) {

            final Map<String, Object> callbackResponse = new HashMap<>();

            callbackResponse.put("confirmation_header", MapValueExtractor.extract(scenario, "confirmation.header"));
            callbackResponse.put("confirmation_body", MapValueExtractor.extract(scenario, "confirmation.body"));

            return MapSerializer.serialize(callbackResponse);

        } else {

            Map<String, Object> caseData = buildCaseData(
                MapValueExtractor.extract(scenario, "caseData"),
                templatesByFilename
            );

            PreSubmitCallbackResponse<AsylumCase> preSubmitCallbackResponse =
                new PreSubmitCallbackResponse<>(
                    objectMapper.readValue(
                        MapSerializer.serialize(caseData),
                        new TypeReference<AsylumCase>() {
                        }
                    )
                );

            preSubmitCallbackResponse.addErrors(MapValueExtractor.extract(scenario, "errors"));

            return objectMapper.writeValueAsString(preSubmitCallbackResponse);
        }
    }

    private Headers getAuthorizationHeaders(Map<String, Object> scenario) {

        String credentials = MapValueExtractor.extract(scenario, "request.credentials");

        if ("LegalRepresentative".equalsIgnoreCase(credentials)) {

            return authorizationHeadersProvider
                .getLegalRepresentativeAuthorization();
        }

        if ("CaseOfficer".equalsIgnoreCase(credentials)) {

            return authorizationHeadersProvider
                .getCaseOfficerAuthorization();
        }

        return new Headers();
    }
}
