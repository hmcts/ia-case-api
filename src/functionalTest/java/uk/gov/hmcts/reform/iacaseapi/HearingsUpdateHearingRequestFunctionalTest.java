package uk.gov.hmcts.reform.iacaseapi;

import io.restassured.http.Header;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.HearingsUpdateHearingRequestMidEventHandler;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.LISTING;

/**
 * This functional test class covers all callback handlers in relation to Update Hearing including.
 * {@link HearingsUpdateHearingRequestMidEventHandler}
 * {@link uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.HearingsUpdateHearingRequestPreparer}
 * {@link uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.HearingsUpdateHearingRequestSubmit}
 */
@Slf4j
@ActiveProfiles("functional")
@DirtiesContext
@Disabled
public class HearingsUpdateHearingRequestFunctionalTest extends CcdCaseCreationTest {

    @BeforeEach
    void getAuthentications() {
        fetchTokensAndUserIds();
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void should_handle_update_hearing_request_about_to_start_successfully(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);

        log.info("caseOfficerToken: " + caseOfficerToken);
        log.info("s2sToken: " + s2sToken);

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
            "IA",
            LISTING,
            result.getCaseData(),
            LocalDateTime.now(),
            "securityClassification",
            null
        );

        Callback<CaseData> callback = new Callback<>(caseDetails, Optional.of(caseDetails), UPDATE_HEARING_REQUEST);
        given(caseApiSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, caseOfficerToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(callback)
            .post("/asylum/ccdAboutToStart")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true);
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void should_fail_to_handle_update_hearing_request_about_to_start_due_to_invalid_authentication(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
            "IA",
            LISTING,
            result.getCaseData(),
            LocalDateTime.now(),
            "securityClassification",
            null
        );

        Callback<CaseData> callback = new Callback<>(caseDetails, Optional.of(caseDetails), UPDATE_HEARING_REQUEST);

        Response response = given(caseApiSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, "invalidToken"))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(callback)
            .post("/asylum/ccdAboutToStart")
            .then()
            .log().all(true)
            .extract().response();

        assertEquals(401, response.getStatusCode());
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void should_handle_update_hearing_request_mid_event_successfully(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
            "IA",
            LISTING,
            result.getCaseData(),
            LocalDateTime.now(),
            "securityClassification",
            null
        );

        Callback<CaseData> callback = new Callback<>(caseDetails, Optional.of(caseDetails), UPDATE_HEARING_REQUEST);

        Response response = given(caseApiSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, caseOfficerToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(callback)
            .post("/asylum/ccdMidEvent")
            .then()
            .log().all(true)
            .extract().response();

        assertEquals(200, response.getStatusCode());
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void should_fail_to_handle_update_hearing_request_mid_event_due_to_invalid_authentication(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
            "IA",
            LISTING,
            result.getCaseData(),
            LocalDateTime.now(),
            "securityClassification",
            null
        );

        Callback<CaseData> callback = new Callback<>(caseDetails, Optional.of(caseDetails), UPDATE_HEARING_REQUEST);

        Response response = given(caseApiSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, "invalidToken"))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(callback)
            .post("/asylum/ccdMidEvent")
            .then()
            .log().all(true)
            .extract().response();

        assertEquals(401, response.getStatusCode());
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void should_handle_update_hearing_request_about_to_submit_successfully(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
            "IA",
            LISTING,
            result.getCaseData(),
            LocalDateTime.now(),
            "securityClassification",
            null
        );

        Callback<CaseData> callback = new Callback<>(caseDetails, Optional.of(caseDetails), UPDATE_HEARING_REQUEST);

        Response response = given(caseApiSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, caseOfficerToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(callback)
            .post("/asylum/ccdAboutToSubmit")
            .then()
            .log().all(true)
            .extract().response();

        assertEquals(200, response.getStatusCode());
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void should_fail_to_handle_update_hearing_request_about_to_submit_due_to_invalid_authentication(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
            "IA",
            LISTING,
            result.getCaseData(),
            LocalDateTime.now(),
            "securityClassification",
            null
        );

        Callback<CaseData> callback = new Callback<>(caseDetails, Optional.of(caseDetails), UPDATE_HEARING_REQUEST);

        Response response = given(caseApiSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, "invalidToken"))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(callback)
            .post("/asylum/ccdAboutToSubmit")
            .then()
            .log().all(true)
            .extract().response();

        assertEquals(401, response.getStatusCode());
    }

}
