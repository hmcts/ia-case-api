package uk.gov.hmcts.reform.iacaseapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.iacaseapi.util.AuthorizationHeadersProvider;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class EndpointSecurityTest {

    @Value("${targetInstance}") private String targetInstance;

    @Autowired private AuthorizationHeadersProvider authorizationHeadersProvider;

    @Test
    public void should_allow_unauthenticated_requests_to_health_check_and_return_200_response_code() {

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        String response = SerenityRest
            .when()
            .get("/health")
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().asString();

        assertThat(response)
            .contains("UP");
    }

    @Test
    public void should_not_allow_unauthenticated_requests_and_return_403_response_code() {

        SerenityRest
            .given()
            .when()
            .get("/")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void should_not_allow_requests_without_valid_service_authorisation_and_return_403_response_code() {

        String invalidServiceToken = "invalid";

        String accessToken = authorizationHeadersProvider
            .getCaseOfficerAuthorization()
            .getValue("Authorization");

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        SerenityRest
            .given()
            .header("ServiceAuthorization", invalidServiceToken)
            .header("Authorization", accessToken)
            .when()
            .get("/")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void should_not_allow_requests_without_valid_user_authorisation_and_return_403_response_code() {

        String serviceToken = authorizationHeadersProvider
            .getCaseOfficerAuthorization()
            .getValue("ServiceAuthorization");

        String invalidAccessToken = "invalid";

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        SerenityRest
            .given()
            .header("ServiceAuthorization", serviceToken)
            .header("Authorization", invalidAccessToken)
            .when()
            .get("/")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
