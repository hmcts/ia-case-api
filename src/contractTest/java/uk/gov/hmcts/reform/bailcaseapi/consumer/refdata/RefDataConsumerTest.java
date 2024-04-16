package uk.gov.hmcts.reform.bailcaseapi.consumer.refdata;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.refdata.CommonDataRefApi;
import uk.gov.hmcts.reform.bailcaseapi.util.ResourceLoader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@PactTestFor(providerName = "referenceData_listOfValues", port = "8991")
@ContextConfiguration(classes = {RefDataConsumerApplication.class})
public class RefDataConsumerTest {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String AUTH_TOKEN = "Bearer someAuthorizationToken";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthorizationToken";

    private final String interpreterLanguageResponse = "response/RefDataInterpreterLanguageResponse.json";
    private final String signLanguageResponse = "response/RefDataSignLanguageResponse.json";

    @Autowired
    CommonDataRefApi commonDataRefApi;

    @Pact(provider = "referenceDataCategoryApi", consumer = "bail_caseApi")
    public RequestResponsePact generateInterpreterLanguageCategoryApiConsumerTest(PactDslWithProvider builder) throws Exception {

        return builder
            .given("A list_of_values InterpreterLanguage for CRD request")
            .uponReceiving("A request for interpreter language")
            .path("/refdata/commondata/lov/categories/InterpreterLanguage")
            .query("serviceId=BFA1&isChildRequired=Y")
            .method("GET")
            .matchHeader(AUTHORIZATION, AUTH_TOKEN)
            .matchHeader(SERVICE_AUTHORIZATION, SERVICE_AUTH_TOKEN)
            .willRespondWith()
            .status(200)
            .body(ResourceLoader.loadJson(interpreterLanguageResponse), "application/json")
            .toPact();
    }

    @Pact(provider = "referenceDataCategoryApi", consumer = "bail_caseApi")
    public RequestResponsePact generateSignLanguageCategoryApiConsumerTest(PactDslWithProvider builder) throws Exception {

        return builder
            .given("A list_of_values InterpreterLanguage for CRD request")
            .uponReceiving("A request for interpreter language")
            .path("/refdata/commondata/lov/categories/SignLanguage")
            .query("serviceId=BFA1&isChildRequired=Y")
            .method("GET")
            .matchHeader(AUTHORIZATION, AUTH_TOKEN)
            .matchHeader(SERVICE_AUTHORIZATION, SERVICE_AUTH_TOKEN)
            .willRespondWith()
            .status(200)
            .headers(ImmutableMap.<String, String>builder().put(HttpHeaders.CONNECTION, "close").build())
            .body(ResourceLoader.loadJson(signLanguageResponse), "application/json")
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generateInterpreterLanguageCategoryApiConsumerTest")
    public void verifyInterpreterLanguageFetch() {
        final CommonDataResponse interpreterLanguageCategories =
            commonDataRefApi.getAllCategoryValuesByCategoryId(
                AUTH_TOKEN, SERVICE_AUTH_TOKEN, "InterpreterLanguage", "BFA1", "Y");

        assertNotNull(interpreterLanguageCategories);
    }

    @Test
    @PactTestFor(pactMethod = "generateSignLanguageCategoryApiConsumerTest")
    public void verifySignLanguageFetch() {
        final CommonDataResponse signLanguageCategories =
            commonDataRefApi.getAllCategoryValuesByCategoryId(
                AUTH_TOKEN, SERVICE_AUTH_TOKEN, "SignLanguage", "BFA1", "Y");

        assertNotNull(signLanguageCategories);
    }
}
