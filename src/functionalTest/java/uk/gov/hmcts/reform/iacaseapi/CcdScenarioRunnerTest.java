package uk.gov.hmcts.reform.iacaseapi;

import static org.junit.Assert.assertFalse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.StreamSupport;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.util.*;
import uk.gov.hmcts.reform.iacaseapi.verifiers.Verifier;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class CcdScenarioRunnerTest {

    @Value("${targetInstance}") private String targetInstance;

    @Autowired private Environment environment;
    @Autowired private AuthorizationHeadersProvider authorizationHeadersProvider;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private List<Verifier> verifiers;

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void scenarios_should_behave_as_specified() throws IOException {

        assertFalse(
            "Verifiers are configured",
            verifiers.isEmpty()
        );

        loadPropertiesIntoMapValueExpander();

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

            final long testCaseId = MapValueExtractor.extractOrDefault(
                scenario,
                "request.input.id",
                ThreadLocalRandom.current().nextInt(1, 9999999 + 1)

            );

            final String requestBody = buildCallbackBody(
                testCaseId,
                MapValueExtractor.extract(scenario, "request.input"),
                templatesByFilename
            );

            final Headers authorizationHeaders = getAuthorizationHeaders(scenario);
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

            verifiers.forEach(verifier ->
                verifier.verify(
                    testCaseId,
                    scenario,
                    expectedResponse,
                    actualResponse
                )
            );
        }

        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");
    }

    private void loadPropertiesIntoMapValueExpander() {

        MutablePropertySources propertySources = ((AbstractEnvironment) environment).getPropertySources();
        StreamSupport
            .stream(propertySources.spliterator(), false)
            .filter(propertySource -> propertySource instanceof EnumerablePropertySource)
            .map(ropertySource -> ((EnumerablePropertySource) ropertySource).getPropertyNames())
            .flatMap(Arrays::stream)
            .forEach(name -> MapValueExpander.ENVIRONMENT_PROPERTIES.setProperty(name, environment.getProperty(name)));
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
        long testCaseId,
        Map<String, Object> input,
        Map<String, String> templatesByFilename
    ) throws IOException {

        Map<String, Object> caseData = buildCaseData(
            MapValueExtractor.extract(input, "caseData"),
            templatesByFilename
        );

        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("id", testCaseId);
        caseDetails.put("jurisdiction", MapValueExtractor.extractOrDefault(input, "jurisdiction", "IA"));
        caseDetails.put("state", MapValueExtractor.extractOrThrow(input, "state"));
        caseDetails.put("case_data", caseData);

        Map<String, Object> callback = new HashMap<>();
        callback.put("event_id", MapValueExtractor.extractOrThrow(input, "eventId"));
        callback.put("case_details", caseDetails);

        return MapSerializer.serialize(callback);
    }

    private String buildCallbackResponseBody(
        Map<String, Object> expectation,
        Map<String, String> templatesByFilename
    ) throws IOException {

        if (MapValueExtractor.extract(expectation, "confirmation") != null) {

            final Map<String, Object> callbackResponse = new HashMap<>();

            callbackResponse.put("confirmation_header", MapValueExtractor.extract(expectation, "confirmation.header"));
            callbackResponse.put("confirmation_body", MapValueExtractor.extract(expectation, "confirmation.body"));

            return MapSerializer.serialize(callbackResponse);

        } else {

            Map<String, Object> caseData = buildCaseData(
                MapValueExtractor.extract(expectation, "caseData"),
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

            preSubmitCallbackResponse.addErrors(MapValueExtractor.extract(expectation, "errors"));

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
