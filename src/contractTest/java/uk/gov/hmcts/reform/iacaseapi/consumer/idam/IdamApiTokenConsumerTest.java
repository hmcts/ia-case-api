package uk.gov.hmcts.reform.iacaseapi.consumer.idam;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.google.common.collect.ImmutableMap;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.Token;

import java.util.Map;

import static org.junit.Assert.assertEquals;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@PactTestFor(providerName = "ia_caseApi", port = "8892")
@ContextConfiguration(classes = {IdamConsumerApplication.class})
@TestPropertySource(locations = {"classpath:application.properties"}, properties = {"idam.baseUrl=http://localhost:8892"})

public class IdamApiTokenConsumerTest {

    @Autowired
    IdamApi idamApi;

    @Pact(provider = "idamApi_oidc", consumer = "ia_caseApi")
    public RequestResponsePact generatePactFragmentToken(PactDslWithProvider builder) throws JSONException {

        Map<String, String> responseheaders = ImmutableMap.<String, String>builder()
                .put("Content-Type", "application/json")
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
    @PactTestFor(pactMethod = "generatePactFragmentToken", pactVersion = PactSpecVersion.V3)
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


    private PactDslJsonBody createAuthResponse() {

        return new PactDslJsonBody()
                .stringType("access_token", "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdjEre")
                .stringType("scope", "openid roles profile");

    }
}
