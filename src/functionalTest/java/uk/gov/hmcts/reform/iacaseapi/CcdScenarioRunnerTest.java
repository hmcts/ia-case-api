package uk.gov.hmcts.reform.iacaseapi;


import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RetryableException;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.fixtures.Fixture;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.RequestUserAccessTokenProvider;
import uk.gov.hmcts.reform.iacaseapi.util.AuthorizationHeadersProvider;
import uk.gov.hmcts.reform.iacaseapi.util.LaunchDarklyFunctionalTestClient;
import uk.gov.hmcts.reform.iacaseapi.util.MapMerger;
import uk.gov.hmcts.reform.iacaseapi.util.MapSerializer;
import uk.gov.hmcts.reform.iacaseapi.util.MapValueExpander;
import uk.gov.hmcts.reform.iacaseapi.util.MapValueExtractor;
import uk.gov.hmcts.reform.iacaseapi.util.StringResourceLoader;
import uk.gov.hmcts.reform.iacaseapi.verifiers.Verifier;

@RunWith(SpringIntegrationSerenityRunner.class)
@Slf4j
@SpringBootTest
@ActiveProfiles("functional")
public class CcdScenarioRunnerTest {

    @Value("${targetInstance}")
    private String targetInstance;

    @Autowired
    private Environment environment;
    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;
    @Autowired
    private MapValueExpander mapValueExpander;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private List<Verifier> verifiers;
    @Autowired
    private List<Fixture> fixtures;

    @Autowired
    private LaunchDarklyFunctionalTestClient launchDarklyFunctionalTestClient;
    private boolean haveAllPassed = true;
    private final ArrayList<String> failedScenarios = new ArrayList<>();
    @MockBean
    RequestUserAccessTokenProvider requestUserAccessTokenProvider;


    @BeforeEach
    public void setUp() {
        MapSerializer.setObjectMapper(objectMapper);
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @BeforeEach
    @SneakyThrows
    void authenticateMe() {
        String accessToken = authorizationHeadersProvider.getCaseOfficerAuthorization().getValue("Authorization");
        Thread.sleep(1000);
        assertNotNull(accessToken);
        when(requestUserAccessTokenProvider.getAccessToken()).thenReturn(accessToken);
    }


    @Test
    public void scenarios_should_behave_as_specified() throws IOException {
        loadPropertiesIntoMapValueExpander();

        for (Fixture fixture : fixtures) {
            fixture.prepare();
        }


        assertFalse(
            "Verifiers are configured",
            verifiers.isEmpty()
        );

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

        log.info((char) 27 + "\033[36m" + "-------------------------------------------------------------------");
        log.info((char) 27 + "\033[33m" + "RUNNING " + scenarioSources.size() + " SCENARIOS");
        log.info((char) 27 + "\033[36m" + "-------------------------------------------------------------------");
        int maxRetries = 3;
        for (String scenarioSource : scenarioSources) {
            String description = "";
            for (int i = 0; i < maxRetries; i++) {
                try {
                    Map<String, Object> scenario = deserializeWithExpandedValues(scenarioSource);
                    final Headers authorizationHeaders = getAuthorizationHeaders(scenario);

                    description = MapValueExtractor.extract(scenario, "description");

                    Object scenarioEnabled = MapValueExtractor.extract(scenario, "enabled") == null
                        ? MapValueExtractor.extract(scenario, "launchDarklyKey")
                        : MapValueExtractor.extract(scenario, "enabled");

                    if (scenarioEnabled == null) {
                        scenarioEnabled = true;
                    } else if (scenarioEnabled instanceof String) {

                        if (String.valueOf(scenarioEnabled).contains("feature")) {

                            String[] keys = ((String) scenarioEnabled).split(":");

                            scenarioEnabled = launchDarklyFunctionalTestClient
                                .getKey(keys[0], authorizationHeaders.getValue("Authorization"))
                                && Boolean.valueOf(keys[1]);
                        } else {
                            scenarioEnabled = Boolean.valueOf((String) scenarioEnabled);
                        }
                    }

                    Object scenarioDisabled = MapValueExtractor.extract(scenario, "disabled");

                    if (scenarioDisabled == null) {
                        scenarioDisabled = false;
                    } else if (scenarioDisabled instanceof String) {
                        scenarioDisabled = Boolean.valueOf((String) scenarioDisabled);
                    }

                    if (!((Boolean) scenarioEnabled) || ((Boolean) scenarioDisabled)) {
                        log.info((char) 27 + "\033[31m" + "SCENARIO: " + description + " **disabled**");
                        break;
                    }

                    log.info((char) 27 + "\033[33m" + "SCENARIO: " + description);

                    Map<String, String> templatesByFilename = StringResourceLoader.load("/templates/*.json");

                    final long scenarioTestCaseId = MapValueExtractor.extractOrDefault(
                        scenario,
                        "request.input.id",
                        -1
                    );

                    final long testCaseId = (scenarioTestCaseId == -1)
                        ? ThreadLocalRandom.current().nextLong(1111111111111111L, 1999999999999999L)
                        : scenarioTestCaseId;

                    final String requestBody = buildCallbackBody(
                        testCaseId,
                        MapValueExtractor.extract(scenario, "request.input"),
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
                            .log().ifError()
                            .log().ifValidationFails()
                            .statusCode(expectedStatus)
                            .and()
                            .extract()
                            .body()
                            .asString();

                    String expectedResponseBody = buildCallbackResponseBody(
                        MapValueExtractor.extract(scenario, "expectation"),
                        templatesByFilename
                    );

                    System.out.println(actualResponseBody);

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
                    break;
                } catch (Error | RetryableException | NullPointerException e) {
                    log.error("Scenario failed with error " + e.getMessage());
                    if (i == maxRetries - 1) {
                        this.failedScenarios.add(description);
                        this.haveAllPassed = false;
                    }
                }
            }
        }

        log.info((char) 27 + "\033[36m" + "-------------------------------------------------------------------");
        log.info((char) 27 + "\033[0m");
        if (!haveAllPassed) {
            throw new AssertionError("Not all scenarios passed.\nFailed scenarios are:\n" + failedScenarios.stream().map(Object::toString).collect(Collectors.joining(";\n")));
        }
    }

    private void loadPropertiesIntoMapValueExpander() {

        MutablePropertySources propertySources = ((AbstractEnvironment) environment).getPropertySources();
        StreamSupport
            .stream(propertySources.spliterator(), false)
            .filter(propertySource -> propertySource instanceof EnumerablePropertySource)
            .map(propertySource -> ((EnumerablePropertySource) propertySource).getPropertyNames())
            .flatMap(Arrays::stream)
            .forEach(name -> MapValueExpander.ENVIRONMENT_PROPERTIES.setProperty(name, environment.getProperty(name)));
    }

    private Map<String, Object> deserializeWithExpandedValues(
        String source
    ) throws IOException {
        Map<String, Object> data = MapSerializer.deserialize(source);
        mapValueExpander.expandValues(data);
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

        LocalDateTime createdDate =
            LocalDateTime.parse(
                MapValueExtractor.extractOrDefault(input, "createdDate", LocalDateTime.now().toString())
            );

        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("id", testCaseId);
        caseDetails.put("jurisdiction", MapValueExtractor.extractOrDefault(input, "jurisdiction", "IA"));
        caseDetails.put("state", MapValueExtractor.extractOrThrow(input, "state"));
        caseDetails.put("security_classification",
            MapValueExtractor.extractOrDefault(input, "securityClassification", "PUBLIC"));
        caseDetails.put("created_date", createdDate);
        caseDetails.put("case_data", caseData);

        Map<String, Object> callback = new HashMap<>();
        callback.put("event_id", MapValueExtractor.extractOrThrow(input, "eventId"));
        String eventPageId = MapValueExtractor.extract(input, "pageId");
        if (eventPageId != null) {
            callback.put("page_id", eventPageId);
        }
        callback.put("case_details", caseDetails);

        if (input.containsKey("caseDataBefore")) {
            Map<String, Object> caseDataBefore = buildCaseData(
                MapValueExtractor.extract(input, "caseDataBefore"),
                templatesByFilename
            );

            Map<String, Object> caseDetailsBefore = new HashMap<>();
            caseDetailsBefore.put("id", testCaseId);
            caseDetailsBefore.put("jurisdiction", MapValueExtractor.extractOrDefault(input, "jurisdiction", "IA"));
            caseDetailsBefore.put("state", MapValueExtractor.extractOrThrow(input, "state"));
            caseDetailsBefore.put("created_date", createdDate);
            caseDetailsBefore.put("case_data", caseDataBefore);
            callback.put("case_details_before", caseDetailsBefore);
        }

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

        return switch (Objects.requireNonNull(credentials).toLowerCase()) {
            case "legalrepresentative" ->
                authorizationHeadersProvider.getLegalRepresentativeAuthorization();
            case "systemuser" ->
                authorizationHeadersProvider.getSystemUserAuthorization();
            case "caseofficer" ->
                authorizationHeadersProvider.getCaseOfficerAuthorization();
            case "adminofficer" ->
                authorizationHeadersProvider.getAdminOfficerAuthorization();
            case "homeofficeapc" ->
                authorizationHeadersProvider.getHomeOfficeApcAuthorization();
            case "homeofficelart" ->
                authorizationHeadersProvider.getHomeOfficeLartAuthorization();
            case "homeofficepou" ->
                authorizationHeadersProvider.getHomeOfficePouAuthorization();
            case "homeofficegeneric" -> authorizationHeadersProvider
                    .getHomeOfficeGenericAuthorization();
            case "legalrepresentativeorga" -> authorizationHeadersProvider
                .getLegalRepresentativeOrgAAuthorization();
            case "legalrepresentativeorgsuccess" -> authorizationHeadersProvider
                .getLegalRepresentativeOrgSuccessAuthorization();
            case "legalrepresentativeorgdeleted" -> authorizationHeadersProvider
                .getLegalRepresentativeOrgDeletedAuthorization();
            case "judge" -> authorizationHeadersProvider
                .getJudgeAuthorization();
            case "citizen" -> authorizationHeadersProvider
                .getCitizenAuthorization();
            default -> new Headers();
        };
    }
}
