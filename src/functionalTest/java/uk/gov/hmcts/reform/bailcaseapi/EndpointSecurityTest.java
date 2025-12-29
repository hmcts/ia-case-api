package uk.gov.hmcts.reform.bailcaseapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import java.util.Arrays;
import java.util.List;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.bailcaseapi.util.AuthorizationHeadersProvider;

@SpringBootTest
@ActiveProfiles("functional")
@DirtiesContext
public class EndpointSecurityTest {

    @Value("${targetInstance}") private String targetInstance;

    private final List<String> callbackEndpoints =
        Arrays.asList(
            "/bail/ccdAboutToStart",
            "/bail/ccdAboutToSubmit",
            "/bail/ccdSubmitted"
        );

    @Autowired private AuthorizationHeadersProvider authorizationHeadersProvider;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_allow_unauthenticated_requests_to_welcome_message_and_return_200_response_code() {

        String response =
            SerenityRest
                .given()
                .when()
                .get("/")
                .then()
                .statusCode(HttpStatus.OK.value())
                .and()
                .extract().body().asString();

        assertThat(response)
            .contains("Welcome");
    }

    @Test
    public void should_allow_unauthenticated_requests_to_health_check_and_return_200_response_code() {

        String response =
            SerenityRest
                .given()
                .when()
                .get("/health")
                .then()
                .statusCode(HttpStatus.OK.value())
                .and()
                .extract().body().asString();

        assertThat(response)
            .contains("UP");
    }

    //@Test
    //public void should_not_allow_unauthenticated_requests_and_return_401_response_code() {
    //
    //    callbackEndpoints.forEach(callbackEndpoint ->
    //
    //                                  SerenityRest
    //                                      .given()
    //                                      .when()
    //                                      .get(callbackEndpoint)
    //                                      .then()
    //                                      .statusCode(HttpStatus.UNAUTHORIZED.value())
    //    );
    //}

    //@Test
    //public void should_not_allow_requests_without_valid_service_authorisation_and_return_401_response_code() {
    //
    //    String invalidServiceToken = "invalid";
    //
    //    String accessToken =
    //        authorizationHeadersProvider
    //            .getCaseOfficerAuthorization()
    //            .getValue("Authorization");
    //
    //    callbackEndpoints.forEach(callbackEndpoint ->
    //
    //                                  SerenityRest
    //                                      .given()
    //                                      .header("ServiceAuthorization", invalidServiceToken)
    //                                      .header("Authorization", accessToken)
    //                                      .when()
    //                                      .get(callbackEndpoint)
    //                                      .then()
    //                                      .statusCode(HttpStatus.UNAUTHORIZED.value())
    //    );
    //}
    //
    //@Test
    //public void should_not_allow_requests_without_valid_user_authorisation_and_return_401_response_code() {
    //
    //    String serviceToken =
    //        authorizationHeadersProvider
    //            .getCaseOfficerAuthorization()
    //            .getValue("ServiceAuthorization");
    //
    //    String invalidAccessToken = "invalid";
    //
    //    callbackEndpoints.forEach(callbackEndpoint ->
    //
    //                                  SerenityRest
    //                                      .given()
    //                                      .header("ServiceAuthorization", serviceToken)
    //                                      .header("Authorization", invalidAccessToken)
    //                                      .when()
    //                                      .get(callbackEndpoint)
    //                                      .then()
    //                                      .statusCode(HttpStatus.UNAUTHORIZED.value())
    //    );
    //}
}
