package uk.gov.hmcts.reform.iacaseapi;

import java.time.LocalDateTime;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADJOURNMENT_DETAILS_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_ADJOURNMENT_WHEN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_REASON_TO_CANCEL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_REASON_TO_UPDATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RELIST_CASE_IMMEDIATELY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.LISTING;

import io.restassured.http.Header;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@Slf4j
@ActiveProfiles("functional")
@Disabled
public class RecordAdjournmentDetailsStateHandlerFunctionalTest extends CcdCaseCreationTest {

    @BeforeEach
    void getAuthentications() {
        fetchTokensAndUserIds();
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void should_handle_record_adjournment_details_update_successfully(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);

        AsylumCase asylumCase = result.getCaseData();

        asylumCase.write(RELIST_CASE_IMMEDIATELY, "Yes");
        asylumCase.write(ADJOURNMENT_DETAILS_HEARING, new DynamicList("1234"));
        asylumCase.write(HEARING_REASON_TO_UPDATE, "reclassified");
        asylumCase.write(LIST_CASE_HEARING_CENTRE, "remoteHearing");
        asylumCase.write(LIST_CASE_HEARING_DATE, "2023-11-28T09:45:00.000");

        asylumCase.write(NEXT_HEARING_DATE, "FirstAvailableDate");
        asylumCase.write(HEARING_ADJOURNMENT_WHEN, "beforeHearingDate");

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

        Callback callback = new Callback<>(caseDetails, Optional.of(caseDetails), RECORD_ADJOURNMENT_DETAILS);
        given(caseApiSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, caseOfficerToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(callback)
            .post("/asylum/ccdAboutToSubmit")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("data.updateHmcRequestSuccess", notNullValue());
    }

    @ParameterizedTest
    @CsvSource({"true,false"})
    void should_handle_record_adjournment_details_cancel_successfully(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);

        AsylumCase asylumCase = result.getCaseData();

        asylumCase.write(RELIST_CASE_IMMEDIATELY, "No");
        asylumCase.write(ADJOURNMENT_DETAILS_HEARING, new DynamicList("1234"));

        asylumCase.write(HEARING_REASON_TO_CANCEL, "reclassified");
        asylumCase.write(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, "a@a.com");

        asylumCase.write(LIST_CASE_HEARING_CENTRE, "remoteHearing");
        asylumCase.write(LIST_CASE_HEARING_DATE, "2023-11-28T09:45:00.000");

        asylumCase.write(NEXT_HEARING_DATE, "FirstAvailableDate");
        asylumCase.write(HEARING_ADJOURNMENT_WHEN, "beforeHearingDate");

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

        Callback callback = new Callback<>(caseDetails, Optional.of(caseDetails), RECORD_ADJOURNMENT_DETAILS);
        given(caseApiSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, caseOfficerToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(callback)
            .post("/asylum/ccdAboutToSubmit")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("data.manualCanHearingRequired", notNullValue());
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void should_fail_handle_record_adjournment_details_state_due_to_missing_fields(boolean isAipJourney) {
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

        Callback callback = new Callback<>(caseDetails, Optional.of(caseDetails), RECORD_ADJOURNMENT_DETAILS);
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

        assertEquals(400, response.getStatusCode());
    }


}
