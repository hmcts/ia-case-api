package uk.gov.hmcts.reform.iacaseapi;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.Maps;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
public class IdamConsumerTest {

    private static final String IDAM_DETAILS_URL = "/details";
    private static final String IDAM_OAUTH2_AUTHORIZE_URL = "/oauth2/authorize";
    private static final String IDAM_OAUTH2_TOKEN_URL = "/oauth2/token";

    private static final String CLIENT_REDIRECT_URI = "/oauth2redirect";

    private static final String ACCESS_TOKEN = "111";

    @BeforeEach
    public void setUp() {
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.config().encoderConfig(new EncoderConfig("UTF-8", "UTF-8"));
    }

    @Pact(provider = "idam_api", consumer = "ia_case_api")
    public RequestResponsePact executeGetIdamAuthCodeAndGet200Response(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        return builder
            .given("Idam returns the auth code ")
            .uponReceiving("Provider receives a POST request from an IA API")
            .path(IDAM_OAUTH2_AUTHORIZE_URL)
            .method(HttpMethod.POST.toString())
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(new PactDslJsonBody()
                .stringType("code", "12345"))
            .toPact();

    }

    @Pact(provider = "idam_api", consumer = "ia_case_api")
    public RequestResponsePact executeGetIdamAuthTokenAndGet200(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        return builder
            .given("Idam successfully returns user details")
            .uponReceiving("Provider receives a GET /code request from an IA API")
            .path(IDAM_OAUTH2_TOKEN_URL)
            .headers(headers)
            .method(HttpMethod.POST.toString())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(new PactDslJsonBody()
                .stringType("access_token", "some-long-access-token")
                .stringType("token_type", "Bearer")
                .stringType("expires_in", "28800"))
            .toPact();

    }

    @Pact(provider = "idam_api", consumer = "ia_case_api")
    public RequestResponsePact executeGetUserDetailsAndGet200(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        return builder
            .given("Idam successfully returns user details")
            .uponReceiving("Provider receives a GET /details request from an IA API")
            .path(IDAM_DETAILS_URL)
            .method(HttpMethod.GET.toString())
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(createUserDetailsResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetUserDetailsAndGet200")
    public void should_get_user_details_with_access_token(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        String actualResponseBody =
            SerenityRest
                .given()
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(mockServer.getUrl() + IDAM_DETAILS_URL)
                .then()
                .statusCode(200)
                .and()
                .extract()
                .body()
                .asString();

        JSONObject response = new JSONObject(actualResponseBody);

        assertThat(actualResponseBody).isNotNull();
        assertThat(response).hasNoNullFieldsOrProperties();
        assertThat(response.getString("id")).isNotBlank();
        assertThat(response.getString("forename")).isNotBlank();
        assertThat(response.getString("surname")).isNotBlank();

        JSONArray rolesArr = new JSONArray(response.getString("roles"));

        assertThat(rolesArr).isNotNull();
        assertThat(rolesArr.length()).isNotZero();
        assertThat(rolesArr.get(0).toString()).isNotBlank();

    }

    @Test
    @PactTestFor(pactMethod = "executeGetIdamAuthCodeAndGet200Response")
    public void should_post_to_oauth2_authorize_and_receive_code_with_200_response(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("response_type", "code");
        body.add("client_id", "ia");
        body.add("redirect_uri", CLIENT_REDIRECT_URI);

        String actualResponseBody =
            SerenityRest
                .given()
                .headers(headers)
                .contentType(ContentType.URLENC)
                .formParams(body)
                .when()
                .post(mockServer.getUrl() + IDAM_OAUTH2_AUTHORIZE_URL)
                .then()
                .statusCode(200)
                .and()
                .extract()
                .asString();

        assertThat(actualResponseBody).isNotNull();

        JSONObject response = new JSONObject(actualResponseBody);
        assertThat(response.get("code").toString()).isNotBlank();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetIdamAuthTokenAndGet200")
    public void should_post_to_oauth2_token_and_receive_code_with_200_response(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();

        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", "some-code");
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", CLIENT_REDIRECT_URI);
        body.add("client_id", "ia");
        body.add("client_secret", "some-client-secret");

        String actualResponseBody =

            SerenityRest
                .given()
                .headers(headers)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .formParams(body)
                .log().all(true)
                .when()
                .post(mockServer.getUrl() + IDAM_OAUTH2_TOKEN_URL)
                .then()
                .statusCode(200)
                .and()
                .extract().response().body()
                .asString();

        JSONObject response = new JSONObject(actualResponseBody);

        assertThat(response).isNotNull();
        assertThat(response.getString("access_token")).isNotBlank();
        assertThat(response.getString("token_type")).isEqualTo("Bearer");
        assertThat(response.getString("expires_in")).isNotBlank();

    }

    private PactDslJsonBody createUserDetailsResponse() {
        PactDslJsonArray array = new PactDslJsonArray().stringValue("caseofficer-ia");

        return new PactDslJsonBody()
            .stringValue("id", "123")
            .stringValue("email", "ia-caseofficer@fake.hmcts.net")
            .stringValue("forename", "Case")
            .stringValue("surname", "Officer")
            .stringValue("roles", array.toString());

    }

}
