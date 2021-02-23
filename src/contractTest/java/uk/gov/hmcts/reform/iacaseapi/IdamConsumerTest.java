package uk.gov.hmcts.reform.iacaseapi;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.Maps;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
public class IdamConsumerTest {

    public static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    public static final String TOKEN_REGEXP = "[a-zA-Z0-9._-]+";
    private static final String IDAM_OPENID_TOKEN_URL = "/o/token";
    private static final String IDAM_OPENID_USERINFO_URL = "/o/userinfo";

    @Pact(provider = "Idam_api", consumer = "ia_case_api")
    public RequestResponsePact executeGetUserInfo(PactDslWithProvider builder) {

        Map<String, Object> params = new HashMap<>();
        params.put("redirect_uri", "http://www.dummy-pact-service.com/callback");
        params.put("client_id", "pact");
        params.put("client_secret", "pactsecret");
        params.put("scope", "openid profile roles");
        params.put("username", "ia-user@email.net");
        params.put("password", "Password12");

        Map<String, String> responseheaders = Maps.newHashMap();
        responseheaders.put("Content-Type", "application/json");

        return builder.given("I have obtained an access_token as a user", params)
                .uponReceiving("IDAM returns user info to the client")
                .path(IDAM_OPENID_USERINFO_URL)
                .headers("Authorization","Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre")
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(responseheaders)
                .body(createUserDetailsResponse())
                .toPact();
    }

    @Pact(provider = "Idam_api", consumer = "ia_case_api")
    public RequestResponsePact executeGetIdamAccessTokenAndGet200(PactDslWithProvider builder) throws JSONException {
        String[] rolesArray = new String[1];
        rolesArray[0] = "citizen";
        Map<String, String> requestheaders = Maps.newHashMap();
        requestheaders.put("Content-Type", "application/x-www-form-urlencoded");

        Map<String, String> responseheaders = Maps.newHashMap();
        responseheaders.put("Content-Type", "application/json");

        Map<String, Object> params = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        params.put("email", "ia-caseofficer@fake.hmcts.net");
        params.put("password", "London01");
        params.put("forename","Case");
        params.put("surname", "Officer");
        params.put("roles", rolesArray);

        return builder
                .given("a user exists", params)
                .uponReceiving("Provider receives a POST /o/token request from an IA API")
                .path(IDAM_OPENID_TOKEN_URL)
                .method(HttpMethod.POST.toString())
                .body("redirect_uri=http%3A%2F%2Fwww.dummy-pact-service.com%2Fcallback"
                        + "&client_id=pact&grant_type=password&username=ia-caseofficer@fake.hmcts.net&password=London01"
                        + "&client_secret=pactsecret&scope=openid profile roles","application/x-www-form-urlencoded")
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(responseheaders)
                .body(createAuthResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetIdamAccessTokenAndGet200")
    public void should_post_to_token_endpoint_and_receive_access_token_with_200_response(MockServer mockServer)
            throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .contentType(ContentType.URLENC)
                        .formParam("redirect_uri", "http://www.dummy-pact-service.com/callback")
                        .formParam("client_id", "pact")
                        .formParam("grant_type", "password")
                        .formParam("username", "ia-caseofficer@fake.hmcts.net")
                        .formParam("password", "London01")
                        .formParam("client_secret", "pactsecret")
                        .formParam("scope", "openid profile roles")
                        .post(mockServer.getUrl() + IDAM_OPENID_TOKEN_URL)
                        .then()
                        .log().all().extract().asString();

        JSONObject response = new JSONObject(actualResponseBody);

        assertThat(response).isNotNull();
    }

    private PactDslJsonBody createAuthResponse() {

        return new PactDslJsonBody()
                .stringType("access_token", "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdjEre")
                .stringType("refresh_token", "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92V")
                .stringType("scope", "openid roles profile")
                .stringType("id_token", "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdjEre")
                .stringType("token_type", "Bearer")
                .stringType("expires_in","28798");
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
}
