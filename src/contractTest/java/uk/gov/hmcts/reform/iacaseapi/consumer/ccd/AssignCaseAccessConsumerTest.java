package uk.gov.hmcts.reform.iacaseapi.consumer.ccd;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;

@PactTestFor(providerName = "acc_manageCaseAssignment", port = "8872")
@TestPropertySource(
    properties = "core_case_data_api_assignments_url=http://localhost:8872")
public class AssignCaseAccessConsumerTest extends CcdCaseAssignmentProviderBaseTest {

    @Pact(provider = "acc_manageCaseAssignment", consumer = "ia_caseApi")
    public RequestResponsePact generatePactFragmentForAssign(PactDslWithProvider builder) throws JSONException, IOException {
        // @formatter:off
        Map<String, String> responseheaders = ImmutableMap.<String, String>builder()
            .put(HttpHeaders.CONNECTION, "close")
            .build();
        return builder
            .given("Assign a user to a case")
            .uponReceiving("A request for that case to be assigned")
            .method("POST")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .body(createJsonObject(ccdCaseAssignment.buildAssignAccessCaseUserMap(CASE_ID, IDAM_ID_OF_USER_CREATING_CASE)))
            .path("/case-assignments")
            .willRespondWith()
            .body(buildAssignCasesResponseDsl())
            .headers(responseheaders)
            .status(HttpStatus.SC_CREATED)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForAssign")
    public void verifyAssignAccessToCase() {

        ccdCaseAssignment.assignAccessToCase(callback);

    }

    private DslPart buildAssignCasesResponseDsl() {
        return newJsonBody((o) -> {
            o.stringType("status_message",
                "Roles Role1,Role2 from the organisation policies successfully assigned to the assignee.");
        }).build();
    }
}
