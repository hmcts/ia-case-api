package uk.gov.hmcts.reform.iacaseapi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static java.nio.charset.StandardCharsets.UTF_8;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileCopyUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseResource;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.util.IdamAuthProvider;
import uk.gov.hmcts.reform.iacaseapi.util.MapValueExpander;


@Slf4j
@SpringBootTest()
@ActiveProfiles("functional")
@Disabled
public class CcdCaseCreationTest {

    @Value("classpath:templates/start-appeal-aip.json")
    protected Resource startAipAppeal;

    @Value("classpath:templates/start-appeal-legalrep.json")
    protected Resource startLegalRepAppeal;

    @Autowired
    protected IdamAuthProvider idamAuthProvider;

    @Autowired
    protected AuthTokenGenerator s2sAuthTokenGenerator;

    protected static RequestSpecification caseApiSpecification;

    private static long legalRepCaseId;
    private static long aipCaseId;
    private static Map<String, JsonNode> legalRepAppealCaseData;

    private static Map<String, JsonNode> aipAppealCaseData;

    private Map<String, Object> caseData;

    protected static String s2sToken;
    protected static String legalRepToken;
    protected static String citizenToken;
    protected String systemUserToken;
    protected static String caseOfficerToken;
    private String legalRepUserId;
    private String citizenUserId;
    private String caseOfficerUserId;
    private String systemUserId;
    public String paymentReference;

    private static final String jurisdiction = "IA";
    private static final String caseType = "Asylum";
    protected static final String AUTHORIZATION = "Authorization";
    protected static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @Value("${targetInstance}")
    protected String targetInstance;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private MapValueExpander mapValueExpander;

    protected void setupForLegalRep() {
        startAppealAsLegalRep();
        submitAppealAsLegalRep();
    }

    protected void setupForAip() {
        startAppealAsCitizen();
        submitAppealAsCitizen();
    }

    protected void fetchTokensAndUserIds() {
        s2sToken = s2sAuthTokenGenerator.generate();

        legalRepToken = idamAuthProvider.getLegalRepToken();
        citizenToken = idamAuthProvider.getCitizenToken();
        caseOfficerToken = idamAuthProvider.getCaseOfficerToken();
        systemUserToken = idamAuthProvider.getSystemUserToken();

        citizenUserId = idamAuthProvider.getUserId(citizenToken);
        legalRepUserId = idamAuthProvider.getUserId(legalRepToken);
        caseOfficerUserId = idamAuthProvider.getUserId(caseOfficerToken);
        systemUserId = idamAuthProvider.getUserId(systemUserToken);

        caseApiSpecification = new RequestSpecBuilder()
            .setBaseUri(targetInstance)
            .setRelaxedHTTPSValidation()
            .build();

        log.info("targetInstance: " + targetInstance);
    }

    private void startAppealAsLegalRep() {
        Map<String, Object> data = getStartAppealData(startLegalRepAppeal);
        data.put("paAppealTypePaymentOption", "payNow");

        mapValueExpander.expandValues(data);

        String eventId = "startAppeal";
        StartEventResponse startEventDetails =
            coreCaseDataApi.startForCaseworker(
                legalRepToken,
                s2sToken,
                legalRepUserId,
                jurisdiction, caseType, eventId);

        Event event = Event.builder().id(eventId).build();

        CaseDataContent content = CaseDataContent.builder()
            .caseReference(null)
            .data(data)
            .event(event)
            .eventToken(startEventDetails.getToken())
            .ignoreWarning(true)
            .build();

        CaseDetails caseDetails =
            coreCaseDataApi.submitForCaseworker(
                legalRepToken,
                s2sToken,
                legalRepUserId,
                jurisdiction, caseType, true, content);

        legalRepCaseId = caseDetails.getId();

    }

    private void submitAppealAsLegalRep() {
        caseData = new HashMap<>();
        caseData.put("decisionHearingFeeOption", "decisionWithHearing");
        caseData.put("hmctsCaseNameInternal", "testCase");

        mapValueExpander.expandValues(caseData);

        String eventId = "submitAppeal";
        StartEventResponse startEventDetails =
            coreCaseDataApi.startEventForCaseWorker(
                legalRepToken,
                s2sToken,
                legalRepUserId,
                jurisdiction,
                caseType,
                String.valueOf(legalRepCaseId),
                eventId);

        Event event = Event.builder().id(eventId).build();
        CaseDataContent content = CaseDataContent.builder()
            .caseReference(String.valueOf(legalRepCaseId))
            .data(caseData)
            .event(event)
            .eventToken(startEventDetails.getToken())
            .ignoreWarning(true)
            .build();

        CaseResource caseResource = coreCaseDataApi.createEvent(
            legalRepToken,
            s2sToken,
            String.valueOf(legalRepCaseId),
            content);

        legalRepAppealCaseData = caseResource.getData();
    }

    private void startAppealAsCitizen() {
        Map<String, Object> data = getStartAppealData(startAipAppeal);

        mapValueExpander.expandValues(data);

        String eventId = "startAppeal";
        StartEventResponse startEventDetails =
            coreCaseDataApi.startForCitizen(
                citizenToken,
                s2sToken,
                citizenUserId,
                jurisdiction, caseType, eventId);

        Event event = Event.builder().id(eventId).build();

        CaseDataContent content = CaseDataContent.builder()
            .caseReference(null)
            .data(data)
            .event(event)
            .eventToken(startEventDetails.getToken())
            .ignoreWarning(true)
            .build();

        CaseDetails caseDetails =
            coreCaseDataApi.submitForCitizen(
                citizenToken,
                s2sToken,
                citizenUserId,
                jurisdiction, caseType, true, content);

        aipCaseId = caseDetails.getId();

    }

    private void submitAppealAsCitizen() {
        caseData = new HashMap<>();
        caseData.put("decisionHearingFeeOption", "decisionWithHearing");

        String eventId = "submitAppeal";
        StartEventResponse startEventDetails =
            coreCaseDataApi.startEventForCitizen(
                citizenToken,
                s2sToken,
                citizenUserId,
                jurisdiction,
                caseType,
                String.valueOf(aipCaseId),
                eventId);

        Event event = Event.builder().id(eventId).build();
        CaseDataContent content = CaseDataContent.builder()
            .caseReference(String.valueOf(aipCaseId))
            .data(caseData)
            .event(event)
            .eventToken(startEventDetails.getToken())
            .ignoreWarning(true)
            .build();

        CaseResource caseResource = coreCaseDataApi.createEvent(
            citizenToken,
            s2sToken,
            String.valueOf(aipCaseId),
            content);

        aipAppealCaseData = caseResource.getData();
    }

    private Map<String, Object> getStartAppealData(Resource appealJson) {

        Map<String, Object> data = Collections.emptyMap();

        try {
            data = new ObjectMapper()
                .readValue(asString(appealJson), new TypeReference<>(){});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return data;
    }

    private String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getLegalRepCaseId() {
        return Long.toString(legalRepCaseId);
    }

    public String getAipCaseId() {
        return Long.toString(aipCaseId);
    }

    public AsylumCase getLegalRepCase() {
        AsylumCase asylumCase = new AsylumCase();

        for (Map.Entry<String, JsonNode> entry : legalRepAppealCaseData.entrySet()) {
            asylumCase.put(entry.getKey(), entry.getValue());
        }

        return asylumCase;
    }

    public AsylumCase getAipCase() {
        AsylumCase asylumCase = new AsylumCase();

        for (Map.Entry<String, JsonNode> entry : aipAppealCaseData.entrySet()) {
            asylumCase.put(entry.getKey(), entry.getValue());
        }

        return asylumCase;
    }
}
