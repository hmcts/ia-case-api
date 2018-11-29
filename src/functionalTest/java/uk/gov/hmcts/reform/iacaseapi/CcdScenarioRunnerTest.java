package uk.gov.hmcts.reform.iacaseapi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.describedAs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.util.*;

@RunWith(SpringRunner.class)
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

        for (String scenarioSource : StringResourceLoader.load("/scenarios/*.json").values()) {

            Map<String, Object> scenario = MapSerializer.deserialize(scenarioSource);
            Map<String, String> templatesByFilename = StringResourceLoader.load("/templates/*.json");

            Headers authorizationHeaders = getAuthorizationHeaders(scenario);
            String requestBody = buildCallbackRequestBody(scenario, templatesByFilename);

            String description = MapValueExtractor.extract(scenario, "description");
            int expectedStatus = MapValueExtractor.extractOrDefault(scenario, "expectation.status", 200);

            String response =
                SerenityRest
                    .given()
                    .headers(authorizationHeaders)
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                    .body(requestBody)
                    .when()
                    .post((String) scenario.get("callback"))
                    .then()
                    .statusCode(
                        describedAs(
                            "Status code is correct (" + description + ")",
                            is(expectedStatus)
                        )
                    )
                    .and()
                    .extract().body().asString();

            String expectedResponseBody = buildExpectedResponseBody(scenario, templatesByFilename);

            assertThat(
                "Response is correct (" + description + ")",
                response,
                equalTo(expectedResponseBody)
            );
        }
    }

    private Headers getAuthorizationHeaders(Map<String, Object> scenario) {

        String credentials = MapValueExtractor.extract(scenario, "credentials");

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

    private String buildCallbackRequestBody(
        Map<String, Object> scenario,
        Map<String, String> templatesByFilename
    ) throws IOException {

        String templateFilename = MapValueExtractor.extract(scenario, "input.caseData.template");

        Map<String, Object> caseData = MapSerializer.deserialize(templatesByFilename.get(templateFilename));
        Map<String, Object> caseDataReplacements = MapValueExtractor.extract(scenario, "input.caseData.replacements");

        if (caseDataReplacements != null) {
            MapMerger.merge(caseData, caseDataReplacements);
        }

        MapValueExpander.expandValues(caseData);

        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("id", MapValueExtractor.extract(scenario, "input.id"));
        caseDetails.put("jurisdiction", MapValueExtractor.extract(scenario, "input.jurisdiction"));
        caseDetails.put("state", MapValueExtractor.extract(scenario, "input.state"));
        caseDetails.put("case_data", caseData);

        Map<String, Object> callback = new HashMap<>();
        callback.put("event_id", MapValueExtractor.extract(scenario, "input.eventId"));
        callback.put("case_details", caseDetails);

        return MapSerializer.serialize(callback);
    }

    private String buildExpectedResponseBody(
        Map<String, Object> scenario,
        Map<String, String> templatesByFilename
    ) throws IOException {

        final String callback = MapValueExtractor.extract(scenario, "callback");
        final Map<String, Object> callbackResponse = new HashMap<>();

        if (callback.endsWith("ccdSubmitted")) {

            callbackResponse.put("confirmation_header", MapValueExtractor.extract(scenario, "expectation.confirmation.header"));
            callbackResponse.put("confirmation_body", MapValueExtractor.extract(scenario, "expectation.confirmation.body"));

            return MapSerializer.serialize(callbackResponse);

        } else {

            String templateFilename = MapValueExtractor.extract(scenario, "expectation.caseData.template");

            Map<String, Object> caseData = MapSerializer.deserialize(templatesByFilename.get(templateFilename));
            Map<String, Object> caseDataReplacements = MapValueExtractor.extract(scenario, "expectation.caseData.replacements");

            if (caseDataReplacements != null) {
                MapMerger.merge(caseData, caseDataReplacements);
            }

            MapValueExpander.expandValues(caseData);

            PreSubmitCallbackResponse<AsylumCase> preSubmitCallbackResponse =
                new PreSubmitCallbackResponse<>(
                    objectMapper.readValue(
                        MapSerializer.serialize(caseData),
                        new TypeReference<AsylumCase>() {
                        }
                    )
                );

            preSubmitCallbackResponse.addErrors(MapValueExtractor.extract(scenario, "expectation.errors"));

            return objectMapper.writeValueAsString(preSubmitCallbackResponse);
        }
    }
}
