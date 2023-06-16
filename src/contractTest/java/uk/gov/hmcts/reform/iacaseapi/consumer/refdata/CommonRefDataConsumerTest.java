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
import uk.gov.hmcts.reform.iacaseapi.idam.IdamApiConsumerApplication;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CommonDataRefApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.ResourceLoader;


@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "commonDataRefApi", port = "8899")
@ContextConfiguration(
    classes = {CommonRefDataApiConsumerApplication.class, IdamApiConsumerApplication.class}
)
@TestPropertySource(
    properties = {"commonData.api.url=http://localhost:8899", "idam.api.url=localhost:5000"}
)
@PactFolder("pacts")
public class CommonRefDataConsumerTest {

    @Autowired
    CommonDataRefApi commonDataRefApi;

    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    private static final String SERVICE_AUTHORIZATION_HEADER = "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";

    private final String validResponseBody = "commonrefdata/CommonRefData.json";



    @Pact(provider = "commonDataRefApi", consumer = "prl_cos")
    public RequestResponsePact generatePactFragmentForCategoryId(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("Common Data")
            .uponReceiving("A Request for Common Data API")
            .method("GET")
            .headers("ServiceAuthorization", SERVICE_AUTHORIZATION_HEADER)
            .headers("Authorization", BEARER_TOKEN)
            .headers("Content-Type", "application/json")
            .path("/refdata/commondata/lov/categories/hearingType")
            .query("serviceId=ABA5&isChildRequired=N")
            .willRespondWith()
            .status(200)
            .body(ResourceLoader.loadJson(validResponseBody), "application/json")
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForCategoryId")
    public void verifyCommonDataDetails() {
        CommonDataResponse allCategoryValuesByCategoryId = commonDataRefApi.getAllCategoryValuesByCategoryId(BEARER_TOKEN,
                                                                                                             SERVICE_AUTHORIZATION_HEADER,
                                                                                                             "hearingType",
                                                                                                             "ABA5",
                                                                                                             "N"
        );

        assertNotNull(allCategoryValuesByCategoryId);
    }

}
