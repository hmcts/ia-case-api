package uk.gov.hmcts.reform.iacaseapi.consumer.idam;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;


@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@PactTestFor(providerName = "ia_caseApi", port = "8892")
@ContextConfiguration(classes = {IdamConsumerApplication.class})
@TestPropertySource(locations = {"classpath:application.properties"}, properties = {"idam.baseUrl=http://localhost:8892"})
public class IdamApiConsumerTest {

    @Autowired
    IdamApi idamApi;
    private static final String AUTH_TOKEN = "Bearer someAuthorizationToken";

    @Pact(provider = "idamApi_oidc", consumer = "ia_caseApi")
    public RequestResponsePact generatePactFragmentUserInfo(PactDslWithProvider builder) throws JSONException {

        Map<String, String> responseheaders = ImmutableMap.<String, String>builder()
            .put(HttpHeaders.CONNECTION, "close")
            .build();

        return builder
            .given("userinfo is requested")
            .uponReceiving("A request for a UserInfo")
            .path("/o/userinfo")
            .method("GET")
            .matchHeader("Authorization", AUTH_TOKEN)
            .matchHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .status(200)
            .body(createUserDetailsResponse())
            .headers(responseheaders)
            .toPact();
    }

    @Pact(provider = "idamApi_oidc", consumer = "ia_caseApi")
    public RequestResponsePact generatePactFragmentToken(PactDslWithProvider builder) throws JSONException {

        Map<String, String> responseheaders = ImmutableMap.<String, String>builder()
            .put("Content-Type", "application/json")
            .put(HttpHeaders.CONNECTION, "close")
            .build();

        return builder
            .given("a token is requested")
            .uponReceiving("Provider receives a POST /o/token request from an IA API")
            .path("/o/token")
            .method(HttpMethod.POST.toString())
            .body("redirect_uri=http%3A%2F%2Fwww.dummy-pact-service.com%2Fcallback"
                    + "&client_id=pact&grant_type=password"
                    + "&username=ia-caseofficer@fake.hmcts.net"
                    + "&password=London01"
                    + "&client_secret=pactsecret"
                    + "&scope=openid profile roles",
                "application/x-www-form-urlencoded")
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(responseheaders)
            .body(createAuthResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentUserInfo")
    public void verifyIdamUserDetailsRolesPactUserInfo() {
        UserInfo userInfo = idamApi.userInfo(AUTH_TOKEN);
        assertEquals("User is not Case Officer", "ia-caseofficer@fake.hmcts.net", userInfo.getEmail());
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentToken")
    public void verifyIdamUserDetailsRolesPactToken() {
        Map<String, String> tokenRequestMap = buildTokenRequestMap();
        Token token = idamApi.token(tokenRequestMap);
        assertEquals("Token is not expected", "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdjEre", token.getAccessToken());
    }

    private Map<String, String> buildTokenRequestMap() {
        Map<String, String> tokenRequestMap = ImmutableMap.<String, String>builder()
            .put("redirect_uri", "http://www.dummy-pact-service.com/callback")
            .put("client_id", "pact")
            .put("grant_type", "password")
            .put("username", "ia-caseofficer@fake.hmcts.net")
            .put("password", "London01")
            .put("client_secret", "pactsecret")
            .put("scope", "openid profile roles")
            .build();
        return tokenRequestMap;
    }


    private PactDslJsonBody createUserDetailsResponse() {
        PactDslJsonArray array = new PactDslJsonArray().stringValue("caseofficer-ia");

        return new PactDslJsonBody()
            .stringType("uid", "1111-2222-3333-4567")
            .stringValue("sub", "ia-caseofficer@fake.hmcts.net")
            .stringValue("givenName", "Case")
            .stringValue("familyName", "Officer")
            .minArrayLike("roles", 1, PactDslJsonRootValue.stringType("caseworker-ia-legalrep-solicitor"), 1)
            .stringType("IDAM_ADMIN_USER");
    }

    private PactDslJsonBody createAuthResponse() {

        return new PactDslJsonBody()
            .stringType("access_token", "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdjEre")
            .stringType("scope", "openid roles profile");

    }

}
