package uk.gov.hmcts.reform.iacaseapi;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.restassured.http.Header;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacaseapi.fixtures.CaseDataFixture;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class ReconcileCaseDataFunctionalTest extends FunctionalTest {

    private String jurisdiction = "IA";
    private String caseType = "Asylum";

    private String legalRepUserToken;
    private String legalRepUserId;

    private CaseDataFixture caseDataFixture;

    private List<String> ccdCaseNumbers;
    private String cases;

    @BeforeEach
    public void setUp() {
        createCase();
        ccdCaseNumbers.clear();
    }

    @Test
    public void should_return_400_status_code_for_a_bad_request() {

        ccdCaseNumbers.add(String.valueOf(caseDataFixture.getCaseId()));
        ccdCaseNumbers.add("1234567890123457");
        ccdCaseNumbers.add("1234567890123458");
        cases = caseListAsString(ccdCaseNumbers, "-");

        Response response = supplementaryDetails(
            cases,
            caseDataFixture.getS2sToken()
        );

        assertThat(response.getStatusCode()).isEqualTo(400);

        assertThatCaseIsInState(caseDataFixture.getCaseId(), "appealSubmitted");
    }

    @Test
    public void should_return_401_status_code_when_missing_s2s_token() {

        ccdCaseNumbers.add(String.valueOf(caseDataFixture.getCaseId()));
        cases = caseListAsString(ccdCaseNumbers, ",");

        Response response = supplementaryDetails(
            cases,
            null
        );

        assertThat(response.getStatusCode()).isEqualTo(401);

        assertThatCaseIsInState(caseDataFixture.getCaseId(), "appealSubmitted");
    }

    @Test
    public void should_return_401_status_code_when_invalid_s2s_token() {

        ccdCaseNumbers.add(String.valueOf(caseDataFixture.getCaseId()));
        cases = caseListAsString(ccdCaseNumbers, ",");

        Response response = supplementaryDetails(
            cases,
            "eyJhbGciOiJIUzUxMi.invalid"
        );

        assertThat(response.getStatusCode()).isEqualTo(401);

        assertThatCaseIsInState(caseDataFixture.getCaseId(), "appealSubmitted");
    }

    @Test
    public void should_return_404_status_code_when_supplementary_details_not_found_for_given_case_numbers() {

        ccdCaseNumbers.add("1234567890123457");
        ccdCaseNumbers.add("1234567890123458");
        cases = caseListAsString(ccdCaseNumbers, ",");

        Response response = supplementaryDetails(
            cases,
            caseDataFixture.getS2sToken()
        );

        assertThat(response.getStatusCode()).isEqualTo(404);

        assertThatCaseIsInState(caseDataFixture.getCaseId(), "appealSubmitted");
    }

    @Test
    public void should_return_200_status_code_when_surname_retrieved_for_given_case_number() {

        ccdCaseNumbers.add(String.valueOf(caseDataFixture.getCaseId()));
        cases = caseListAsString(ccdCaseNumbers, ",");

        Response response = supplementaryDetails(
            cases,
            caseDataFixture.getS2sToken()
        );

        assertThat(response.getStatusCode()).isEqualTo(200);

        assertThatCaseIsInState(caseDataFixture.getCaseId(), "appealSubmitted");
    }

    @Test
    public void should_return_200_status_code_when_surname_retrieved_for_given_case_number_duplicated_ccd_ids() {

        ccdCaseNumbers.add(String.valueOf(caseDataFixture.getCaseId()));
        ccdCaseNumbers.add(String.valueOf(caseDataFixture.getCaseId()));
        ccdCaseNumbers.add(String.valueOf(caseDataFixture.getCaseId()));

        cases = caseListAsString(ccdCaseNumbers, ",");

        Response response = supplementaryDetails(
            cases,
            caseDataFixture.getS2sToken()
        );

        assertThat(response.getStatusCode()).isEqualTo(200);

        assertThatCaseIsInState(caseDataFixture.getCaseId(), "appealSubmitted");
    }

    @Test
    public void should_return_206_status_code_when_partial_match_found_for_given_case_numbers() {

        ccdCaseNumbers.add(String.valueOf(caseDataFixture.getCaseId()));
        ccdCaseNumbers.add("1234567890123457");
        ccdCaseNumbers.add("1234567890123458");
        cases = caseListAsString(ccdCaseNumbers, ",");

        Response response = supplementaryDetails(
            cases,
            caseDataFixture.getS2sToken()
        );

        assertThat(response.getStatusCode()).isEqualTo(206);

        assertThatCaseIsInState(caseDataFixture.getCaseId(), "appealSubmitted");
    }

    private void createCase() {

        ccdCaseNumbers = new ArrayList<>();
        cases = "";

        legalRepUserToken = idamAuthProvider.getLegalRepToken();
        legalRepUserId = idamService.getUserInfo(legalRepUserToken).getUid();

        caseDataFixture = new CaseDataFixture(
            ccdApi,
            objectMapper,
            s2sAuthTokenGenerator,
            minimalAppealStarted,
            idamAuthProvider,
            mapValueExpander
        );

        caseDataFixture.startAppeal();
        caseDataFixture.submitAppeal();
    }

    private Response supplementaryDetails(String cases, String serviceToken) {
        return given(requestSpecification)
            .when()
            .header(new Header("Authorization", caseDataFixture.getLegalRepToken()))
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

        String casesToString = "";
        String cases = "";

        if (!ccdCaseNumbers.isEmpty()) {
            for (String ccdCaseNumber : ccdCaseNumbers) {
                casesToString += "\"" + ccdCaseNumber + "\"" + delimiter;
            }
            cases = casesToString.substring(0, casesToString.length() - 1);
        }

        return cases;
    }

    private void assertThatCaseIsInState(long caseId, String state) {

        await().pollInterval(2, SECONDS).atMost(60, SECONDS).until(() ->
                                                                       ccdApi.get(
                                                                           legalRepUserToken,
                                                                           caseDataFixture.getS2sToken(),
                                                                           legalRepUserId,
                                                                           jurisdiction,
                                                                           caseType,
                                                                           String.valueOf(caseId)
                                                                       ).getState().equals(state)
        );
    }
}
