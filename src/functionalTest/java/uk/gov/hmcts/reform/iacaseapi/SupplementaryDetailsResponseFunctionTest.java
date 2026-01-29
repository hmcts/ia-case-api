package uk.gov.hmcts.reform.iacaseapi;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("functional")
public class SupplementaryDetailsResponseFunctionTest extends CaseAccessFunctionalTest {

    @Test
    public void should_allow_unauthorized_requests_and_return_401_response_code() {
        fetchTokensAndUserIds();
        given(caseApiSpecification)
            .when()
            .get("/supplementary-details")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
