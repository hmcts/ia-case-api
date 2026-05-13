package uk.gov.hmcts.reform.iacaseapi.consumer.idam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import java.text.MessageFormat;
import java.util.List;
import org.apache.http.client.fluent.Executor;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamClientApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.User;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@PactTestFor(providerName = "ia_caseApi", port = "8892")
@SpringJUnitConfig(classes = {IdamConsumerApplication.class})
@TestPropertySource(locations = {"classpath:application.properties"}, properties = {"idam.apiUrl=http://localhost:8892"})
public class IdamClientApiConsumerTest {

    @Autowired
    IdamClientApi idamClientApi;
    private static final String AUTH_TOKEN = "Bearer someAuthorizationToken";
    private static final String USER_ID = "1111-2222-3333-4567";
    private static final String EMAIL = "ia-caseofficer%40fake.hmcts.net";
    private static final String QUERY = MessageFormat.format("id:{0}", USER_ID);

    @BeforeEach
    public void prepareTest() throws Exception {
        Thread.sleep(2000);
    }

    @AfterEach
    void teardown() {
        Executor.closeIdleConnections();
    }

    private DslPart createUserResponseEntityResponse() {
        return PactDslJsonArray.arrayEachLike()
            .stringType("id", USER_ID)
            .stringValue("email", EMAIL)
            .stringValue("forename", "Case")
            .stringValue("surname", "Officer")
            .booleanType("active", true)
            .minArrayLike("roles", 1, PactDslJsonRootValue.stringType("citizen"), 1)
            .closeObject();
    }

    @Pact(provider = "idamClientApi_oidc", consumer = "ia_caseApi")
    public V4Pact generatePactFragmentGetUser(PactDslWithProvider builder) throws JSONException {
        return builder
            .given("getUser is requested")
            .uponReceiving("A request for a GetUser")
            .path("/api/v1/users")
            .method("GET")
            .matchQuery("query", QUERY)
            .matchHeader("Authorization", AUTH_TOKEN)
            .matchHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .status(200)
            .body(createUserResponseEntityResponse())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentGetUser")
    public void verifyIdamUserPactGetUser() {
        List<User> userList = idamClientApi.getUser(AUTH_TOKEN, QUERY).getBody();
        assertNotNull(userList);
        assertFalse(userList.isEmpty());
        User userInfo = userList.getFirst();
        assertEquals(EMAIL, userInfo.getEmail());
        assertEquals(USER_ID, userInfo.getId());
        assertEquals("Case", userInfo.getForename());
        assertEquals("Officer", userInfo.getSurname());
    }
}
