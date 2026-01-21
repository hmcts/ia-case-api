package uk.gov.hmcts.reform.bailcaseapi;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class SmokeTest {
    private final String targetInstance =
        StringUtils.defaultIfBlank(System.getenv("TEST_URL"), "http://localhost:4550");

    @Test
    public void should_check_services_return_overall_health() {
        RequestSpecification requestSpecification = new RequestSpecBuilder()
            .setBaseUri(targetInstance)
            .setRelaxedHTTPSValidation()
            .build();

        Response response = given(requestSpecification)
            .when()
            .get("/health")
            .then()
            .extract().response();

        switch (response.getStatusCode()) {
            case 200:
                assertThat(response.getBody().asString().contains("UP"));
                break;
            default:
                throw new IllegalStateException("Issues with the downstream services");

        }

    }

}
