package uk.gov.hmcts.reform.iacaseapi;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import io.restassured.http.Header;
import io.restassured.response.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacaseapi.fixtures.Fixture;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.RequestUserAccessTokenProvider;
import uk.gov.hmcts.reform.iacaseapi.util.AuthorizationHeadersProvider;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class ReconcileCaseDataFunctionalTest extends CaseAccessFunctionalTest {
    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;
    @MockBean
    RequestUserAccessTokenProvider requestUserAccessTokenProvider;
    private final List<String> ccdCaseNumbers = new ArrayList<>();
    private String cases;
    private Case legalRepCase;

    @BeforeEach
    public void setUp() throws IOException, InterruptedException {
        String accessToken = authorizationHeadersProvider.getCaseOfficerAuthorization().getValue("Authorization");
        Thread.sleep(1000);
        assertNotNull(accessToken);
        when(requestUserAccessTokenProvider.getAccessToken()).thenReturn(accessToken);
        for (Fixture fixture : fixtures) {
            fixture.prepare();
        }
        fetchTokensAndUserIds();
        legalRepCase = createAndGetCase(false);
        ccdCaseNumbers.clear();
    }

    @Test
    public void should_return_400_status_code_for_a_bad_request() {

        ccdCaseNumbers.add(String.valueOf(legalRepCase.getCaseId()));
        ccdCaseNumbers.add("1234567890123457");
        ccdCaseNumbers.add("1234567890123458");
        cases = caseListAsString(ccdCaseNumbers, "-");

        Response response = supplementaryDetails(
            cases,
            s2sToken
        );

        assertThat(response.getStatusCode()).isEqualTo(400);

        assertThatCaseIsInState(legalRepCase.getCaseId(), "appealSubmitted");
    }

    @Test
    public void should_return_401_status_code_when_missing_s2s_token() {

        ccdCaseNumbers.add(String.valueOf(legalRepCase.getCaseId()));
        cases = caseListAsString(ccdCaseNumbers, ",");

        Response response = supplementaryDetails(
            cases,
            null
        );

        assertThat(response.getStatusCode()).isEqualTo(401);

        assertThatCaseIsInState(legalRepCase.getCaseId(), "appealSubmitted");
    }

    @Test
    public void should_return_401_status_code_when_invalid_s2s_token() {

        ccdCaseNumbers.add(String.valueOf(legalRepCase.getCaseId()));
        cases = caseListAsString(ccdCaseNumbers, ",");

        Response response = supplementaryDetails(
            cases,
            "eyJhbGciOiJIUzUxMi.invalid"
        );

        assertThat(response.getStatusCode()).isEqualTo(401);

        assertThatCaseIsInState(legalRepCase.getCaseId(), "appealSubmitted");
    }

    @Test
    public void should_return_404_status_code_when_supplementary_details_not_found_for_given_case_numbers() {

        ccdCaseNumbers.add("1234567890123457");
        ccdCaseNumbers.add("1234567890123458");
        cases = caseListAsString(ccdCaseNumbers, ",");

        Response response = supplementaryDetails(
            cases,
            s2sToken
        );

        assertThat(response.getStatusCode()).isEqualTo(404);

        assertThatCaseIsInState(legalRepCase.getCaseId(), "appealSubmitted");
    }

    @Test
    public void should_return_200_status_code_when_surname_retrieved_for_given_case_number() {

        ccdCaseNumbers.add(String.valueOf(legalRepCase.getCaseId()));
        cases = caseListAsString(ccdCaseNumbers, ",");

        Response response = supplementaryDetails(
            cases,
            s2sToken
        );

        assertThat(response.getStatusCode()).isEqualTo(200);

        assertThatCaseIsInState(legalRepCase.getCaseId(), "appealSubmitted");
    }

    @Test
    public void should_return_200_status_code_when_surname_retrieved_for_given_case_number_duplicated_ccd_ids() {

        ccdCaseNumbers.add(String.valueOf(legalRepCase.getCaseId()));
        ccdCaseNumbers.add(String.valueOf(legalRepCase.getCaseId()));
        ccdCaseNumbers.add(String.valueOf(legalRepCase.getCaseId()));

        cases = caseListAsString(ccdCaseNumbers, ",");

        Response response = supplementaryDetails(
            cases,
            s2sToken
        );

        assertThat(response.getStatusCode()).isEqualTo(200);

        assertThatCaseIsInState(legalRepCase.getCaseId(), "appealSubmitted");
    }

    @Test
    public void should_return_206_status_code_when_partial_match_found_for_given_case_numbers() {

        ccdCaseNumbers.add(String.valueOf(legalRepCase.getCaseId()));
        ccdCaseNumbers.add("1234567890123457");
        ccdCaseNumbers.add("1234567890123458");
        cases = caseListAsString(ccdCaseNumbers, ",");

        Response response = supplementaryDetails(
            cases,
            s2sToken
        );

        assertThat(response.getStatusCode()).isEqualTo(206);

        assertThatCaseIsInState(legalRepCase.getCaseId(), "appealSubmitted");
    }

    private Response supplementaryDetails(String cases, String serviceToken) {
        return given(caseApiSpecification)
            .when()
            .header(new Header("Authorization", legalRepToken))
            .header(new Header("ServiceAuthorization", serviceToken))
            .contentType("application/json")
            .body("{\"ccd_case_numbers\":["
                + cases
                + "]}")
            .post("/supplementary-details")
            .then()
            .extract()
            .response();
    }

    private String caseListAsString(List<String> ccdCaseNumbers, String delimiter) {

        StringBuilder casesToString = new StringBuilder();
        String cases = "";

        if (!ccdCaseNumbers.isEmpty()) {
            for (String ccdCaseNumber : ccdCaseNumbers) {
                casesToString.append("\"").append(ccdCaseNumber).append("\"").append(delimiter);
            }
            cases = casesToString.substring(0, casesToString.length() - 1);
        }

        return cases;
    }

    private void assertThatCaseIsInState(long caseId, String state) {

        await().pollInterval(2, SECONDS).atMost(60, SECONDS).until(() ->
            ccdApi.get(
                legalRepToken,
                s2sToken,
                legalRepUserId,
                jurisdiction,
                caseType,
                String.valueOf(caseId)
            ).getState().equals(state)
        );
    }
}
