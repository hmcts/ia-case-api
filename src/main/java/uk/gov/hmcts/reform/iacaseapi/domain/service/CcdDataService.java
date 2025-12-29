package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_COMPANY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_COMPANY_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_PHONE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_REFERENCE;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUK;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdDataApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdentityManagerResponseException;

@Service
@Slf4j
public class CcdDataService {

    private final CcdDataApi ccdDataApi;
    private final IdamService idamService;
    private final AuthTokenGenerator serviceAuthorization;
    private static final String CASE_TYPE_ASYLUM = "Asylum";
    private static final String CASE_TYPE_BAIL = "Bail";
    private static final String JURISDICTION = "IA";

    public CcdDataService(CcdDataApi ccdDataApi,
                          IdamService idamService,
                          AuthTokenGenerator serviceAuthorization) {

        this.ccdDataApi = ccdDataApi;
        this.idamService = idamService;
        this.serviceAuthorization = serviceAuthorization;
    }

    public SubmitEventDetails retriggerWaTasks(String caseId) {
        Tokens tokens = getTokens(caseId, Event.RE_TRIGGER_WA_TASKS);

        final StartEventDetails<AsylumCase> startEventDetails =
            getCase(
                tokens,
                caseId,
                Event.RE_TRIGGER_WA_TASKS
            );
        log.info("Case details found for the caseId: {}", caseId);

        return submitEvent(
            tokens,
            caseId,
            startEventDetails,
            Event.RE_TRIGGER_WA_TASKS,
            new HashMap<>()
        );
    }

    private Tokens getTokens(String caseId, Event event) {
        Tokens tokens = new Tokens();
        try {
            String userToken = idamService.getServiceUserToken();
            tokens.setUserToken(userToken);
            log.info("System user token has been generated for event: {}, caseId: {}.", event, caseId);

            tokens.setS2sToken(serviceAuthorization.generate());

            log.info("S2S token has been generated for event: {}, caseId: {}.", event, caseId);

            tokens.setUid(idamService.getUserInfo(userToken).getUid());
            log.info("System user id has been fetched for event: {}, caseId: {}.", event, caseId);

        } catch (IdentityManagerResponseException ex) {

            log.error("Unauthorized access to getCaseById", ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }
        return tokens;
    }

    private StartEventDetails<AsylumCase> getCase(Tokens tokens, String caseId, Event event) {
        return ccdDataApi.startEvent(
            tokens.getUserToken(),
            tokens.getS2sToken(),
            tokens.getUid(),
            CcdDataService.JURISDICTION,
            CcdDataService.CASE_TYPE_ASYLUM,
            caseId,
            event.toString()
        );
    }

    private SubmitEventDetails submitEvent(
        Tokens tokens, String caseId, StartEventDetails<AsylumCase> startEventDetails, Event event, Map<String, Object> caseData) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", event.toString());
        CaseDataContent request =
            new CaseDataContent(caseId, caseData, eventData, startEventDetails.getToken(), true);

        return ccdDataApi.submitEvent(tokens.getUserToken(), tokens.getS2sToken(), caseId, request);

    }


    public SubmitEventDetails clearLegalRepDetails(Callback<BailCase> callback) {
        CaseDetails<BailCase> caseDetails = callback.getCaseDetails();
        Event event = Event.CLEAR_LEGAL_REPRESENTATIVE_DETAILS;
        String caseId = String.valueOf(caseDetails.getId());
        String jurisdiction = caseDetails.getJurisdiction();
        Tokens tokens = getTokens(caseId, event);

        final StartEventDetails<BailCase> startEventDetails = ccdDataApi.startBailEvent(
            tokens.getUserToken(),
            tokens.getS2sToken(),
            tokens.getUid(),
            jurisdiction, CASE_TYPE_BAIL,
            caseId,
            event.toString()
        );
        // Get case details by Id
        log.info("Case details found for the caseId: {}", caseId);

        if (isLegalRepDetailsMissing(startEventDetails.getCaseDetails().getCaseData())) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Legal Rep Details not found for the caseId: " + caseId
            );
        }

        // Clear Legal Rep Details
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(LEGAL_REP_NAME.value(), "");
        caseData.put(LEGAL_REP_FAMILY_NAME.value(), "");
        caseData.put(LEGAL_REP_COMPANY.value(), "");
        caseData.put(LEGAL_REP_PHONE.value(), "");
        caseData.put(LEGAL_REP_REFERENCE.value(), "");
        caseData.put(LEGAL_REP_COMPANY_ADDRESS.value(), "");
        caseData.put(LEGAL_REP_EMAIL_ADDRESS.value(), "");
        caseData.put(IS_LEGALLY_REPRESENTED_FOR_FLAG.value(), "No");

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", event.toString());

        SubmitEventDetails submitEventDetails = submitEvent(
            tokens.getUserToken(), tokens.getS2sToken(), caseId, caseData, eventData,
            startEventDetails.getToken(), true
        );

        log.info(
            "Legal Rep Details cleared for the caseId: {}, Status: {}, Message: {}", caseId,
            submitEventDetails.getCallbackResponseStatusCode(), submitEventDetails.getCallbackResponseStatus()
        );

        return submitEventDetails;
    }

    private SubmitEventDetails submitEvent(
        String userToken, String s2sToken, String caseId, Map<String, Object> data,
        Map<String, Object> eventData, String eventToken, boolean ignoreWarning) {

        CaseDataContent request =
            new CaseDataContent(caseId, data, eventData, eventToken, ignoreWarning);

        return ccdDataApi.submitEvent(userToken, s2sToken, caseId, request);

    }

    private boolean isLegalRepDetailsMissing(BailCase bailCase) {

        return bailCase.read(LEGAL_REP_NAME, String.class).isEmpty()
            && bailCase.read(LEGAL_REP_EMAIL_ADDRESS, String.class).isEmpty()
            && bailCase.read(LEGAL_REP_PHONE, String.class).isEmpty()
            && bailCase.read(LEGAL_REP_COMPANY, String.class).isEmpty()
            && bailCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUK.class).isEmpty()
            && bailCase.read(LEGAL_REP_REFERENCE, String.class).isEmpty();
    }

    @Data
    private static class Tokens {
        private String userToken;
        private String s2sToken;
        private String uid;
    }

}
