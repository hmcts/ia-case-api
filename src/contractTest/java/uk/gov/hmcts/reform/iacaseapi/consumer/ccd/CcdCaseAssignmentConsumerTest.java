package uk.gov.hmcts.reform.iacaseapi.consumer.ccd;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import java.io.IOException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@PactTestFor(providerName = "ccdDataStoreAPI_caseAssignedUserRoles", port = "8871")
@TestPropertySource(
    properties = "core_case_data_api_assignments_url=http://localhost:8871")
public class CcdCaseAssignmentConsumerTest extends CcdCaseAssignmentProviderBaseTest {

    @Pact(provider = "ccdDataStoreAPI_caseAssignedUserRoles", consumer = "ia_caseApi")
    public RequestResponsePact generatePactFragmentForRemove(PactDslWithProvider builder) throws IOException {
        // @formatter:off
        return builder
            .given("A User Role exists for a Case")
            .uponReceiving("A Request to remove a User Role")
            .method("DELETE")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/case-users")
            .body(createJsonObject(ccdCaseAssignment.buildRevokeAccessPayload("some-org-identifier", CASE_ID, IDAM_ID_OF_USER_CREATING_CASE)))
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForRemove")
    public void verifyRemoveRole() {

        ccdCaseAssignment.revokeAccessToCase(callback, "some-org-identifier");

    }
}
