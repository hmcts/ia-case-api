package uk.gov.hmcts.reform.iacaseapi;

import java.time.LocalDateTime;
import java.util.Optional;
import static io.restassured.RestAssured.given;
import static java.lang.Long.parseLong;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.LISTING;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@Slf4j
@ActiveProfiles("functional")
public class RecordAdjournmentDetailsPreparerFunctionalTest extends CcdCaseCreationTest {

    @BeforeEach
    void checkCaseExists() {
        fetchTokensAndUserIds();
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void should_handle_update_hearing_request_mid_event_successfully(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);

        log.info("caseOfficerToken: " + caseOfficerToken);
        log.info("s2sToken: " + s2sToken);

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.caseId,
            "IA",
            LISTING,
            result.caseData,
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
            .post("/asylum/ccdMidEvent")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true);
    }

    private record Case(Long caseId, AsylumCase caseData) {
    }

    @NotNull
    private Case createAndGetCase(boolean isAipJourney) {
        Long caseId;
        AsylumCase caseData;
        if (isAipJourney) {
            setupForAip();
            caseData = getAipCase();
            caseId = parseLong(getAipCaseId());
        } else {
            setupForLegalRep();
            caseData = getLegalRepCase();
            caseId = parseLong(getLegalRepCaseId());
        }

        Case result = new Case(caseId, caseData);

        return result;
    }

}
