package uk.gov.hmcts.reform.iacaseapi.consumer.refdata;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.iacaseapi.consumer.idam.IdamConsumerApplication;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CommonDataRefApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.ResourceLoader;


@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "commonDataRefApi", port = "8899")
@ContextConfiguration(
    classes = {CommonRefDataApiConsumerApplication.class, IdamConsumerApplication.class}
)
@TestPropertySource(
    properties = {"commonData.api.url=http://localhost:8899", "idam.api.url=localhost:5000"}
)
@PactFolder("pacts")
public class CommonRefDataConsumerTest {

    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";

    @Autowired
    CommonDataRefApi commonDataRefApi;

    private final String validResponseBody = "commonrefdata/CommonRefData.json";

    @Pact(provider = "commonDataRefApi", consumer = "ia_caseApi")
    public RequestResponsePact generatePactFragmentForCategoryId(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("Common Data")
            .uponReceiving("A Request for Common Data API")
            .method("GET")
            .headers("ServiceAuthorization", SERVICE_AUTH_TOKEN)
            .headers("Authorization", AUTHORIZATION_TOKEN)
            .headers("Content-Type", "application/json")
            .path("/refdata/commondata/lov/categories/InterpreterLanguage")
            .query("serviceId=BFA1&isChildRequired=Y")
            .willRespondWith()
            .status(200)
            .body(ResourceLoader.loadJson(validResponseBody), "application/json")
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForCategoryId")
    public void verifyCommonDataDetails() {
        CommonDataResponse allCategoryValuesByCategoryId = commonDataRefApi.getAllCategoryValuesByCategoryId(
            AUTHORIZATION_TOKEN,
            SERVICE_AUTH_TOKEN,
            "hearingType",
            "BFA1",
            "Y"
        );

        assertNotNull(allCategoryValuesByCategoryId);
    }

}
