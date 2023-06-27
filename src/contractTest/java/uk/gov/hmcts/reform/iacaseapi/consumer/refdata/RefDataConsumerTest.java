package uk.gov.hmcts.reform.iacaseapi.consumer.refdata;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.CaseWorkerProfile;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.refdata.UserIds;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata.CommonDataRefApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.refdata.RefDataCaseWorkerApi;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@PactTestFor(providerName = "referenceData_caseworkerRefUsers", port = "8991")
@ContextConfiguration(classes = {RefDataConsumerApplication.class})
public class RefDataConsumerTest {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String AUTH_TOKEN = "Bearer someAuthorizationToken";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthorizationToken";
    private static final String someActorId = "some actor id";

    @Autowired
    RefDataCaseWorkerApi refDataCaseWorkerApi;

    @Autowired
    CommonDataRefApi commonDataRefApi;

    @Pact(provider = "referenceData_caseworkerRefUsers", consumer = "ia_caseApi")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) throws JSONException, JsonProcessingException {

        return builder
            .given("A list of users for CRD request")
            .uponReceiving("A request for caseworkers")
            .path("/refdata/case-worker/users/fetchUsersById")
            .method("POST")
            .matchHeader(AUTHORIZATION, AUTH_TOKEN)
            .matchHeader(SERVICE_AUTHORIZATION, SERVICE_AUTH_TOKEN)
            .body(new ObjectMapper().writeValueAsString(getUserIds()))
            .willRespondWith()
            .status(200)
            .body(buildCaseworkerListResponsePactDsl())
            .toPact();
    }

    @Pact(provider = "commonDataRefApi", consumer = "ia_caseApi")
    public RequestResponsePact generatePactFragmentForCategoryId(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("Common Data")
            .uponReceiving("A Request for Common Data API")
            .method("GET")
            .headers(AUTHORIZATION, AUTH_TOKEN)
            .headers(SERVICE_AUTHORIZATION, SERVICE_AUTH_TOKEN)
            .headers("Content-Type", "application/json")
            .path("/refdata/commondata/lov/categories/InterpreterLanguage")
            .query("serviceId=BFA1&isChildRequired=Y")
            .willRespondWith()
            .status(200)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragment")
    public void verifyCaseworkersFetch() {

        List<CaseWorkerProfile> caseWorkerProfiles = refDataCaseWorkerApi.fetchUsersById(
            AUTH_TOKEN,
            SERVICE_AUTH_TOKEN,
            getUserIds());
        assertEquals("firstName", caseWorkerProfiles.get(0).getFirstName());

    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForCategoryId")
    public void verifyCommonDataDetails() {
        CommonDataResponse allCategoryValuesByCategoryId = commonDataRefApi.getAllCategoryValuesByCategoryId(
            AUTH_TOKEN,
            SERVICE_AUTH_TOKEN,
            "InterpreterLanguage",
            "BFA1",
            "Y"
        );

        assertNotNull(allCategoryValuesByCategoryId);
    }

    @NotNull
    private UserIds getUserIds() {
        return new UserIds(List.of(someActorId));
    }

    private DslPart buildCaseworkerListResponsePactDsl() {
        return newJsonArray(o -> {
            o.object(ob -> ob
                .stringType("first_name",
                    "firstName")
                .stringType("last_name",
                    "lastName")
            );
        }).build();
    }
}
