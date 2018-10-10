package uk.gov.hmcts.reform.iacaseapi.events;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class WelcomeTest {

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:8090"
        );

    @Test
    public void should_welcome_upon_root_request_with_200_response_code() {

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        String response = RestAssured
            .given()
            .when()
            .get("/")
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().asString();

        assertThat(response)
            .contains("Welcome to Immigration & Asylum case API");
    }
}
