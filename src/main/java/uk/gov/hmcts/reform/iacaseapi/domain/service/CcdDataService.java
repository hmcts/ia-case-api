package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdDataApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdentityManagerResponseException;

@Service
@Slf4j
public class CcdDataService {

    private final CcdDataApi ccdDataApi;
    private final IdamService idamService;
    private final AuthTokenGenerator serviceAuthorization;
    private static final String CASE_TYPE = "Asylum";
    private static final String JURISDICTION = "IA";

    public CcdDataService(CcdDataApi ccdDataApi,
                          IdamService idamService,
                          AuthTokenGenerator serviceAuthorization) {

        this.ccdDataApi = ccdDataApi;
        this.idamService = idamService;
        this.serviceAuthorization = serviceAuthorization;
    }

    public SubmitEventDetails raiseEvent(long caseId, Event eventName) {
        return raiseEvent(String.valueOf(caseId), eventName);
    }

    public SubmitEventDetails raiseEvent(String caseId, Event eventName) {
        log.info("Event name: {}", eventName);
        Tokens tokens = getTokens(caseId, eventName);

        final StartEventDetails startEventDetails =
            getCase(
                tokens,
                caseId,
                eventName
            );
        log.debug("Case details found for caseId {}", caseId);

        return submitEvent(
            tokens,
            caseId,
            startEventDetails,
            eventName,
            Collections.emptyMap()
        );
    }

    private Tokens getTokens(String caseId, Event event) {
        Tokens tokens = new Tokens();
        try {
            String userToken = idamService.getServiceUserToken();
            tokens.setUserToken(userToken);
            log.debug("System user token has been generated for event: {}, caseId: {}.", event, caseId);
            tokens.setS2sToken(serviceAuthorization.generate());
            log.debug("S2S token has been generated for event: {}, caseId: {}.", event, caseId);
            tokens.setUid(idamService.getUserInfo(userToken).getUid());
            log.debug("System user id has been fetched for event: {}, caseId: {}.", event, caseId);

        } catch (IdentityManagerResponseException ex) {

            log.error("Unauthorized access to getCaseById", ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }
        return tokens;
    }

    private StartEventDetails getCase(Tokens tokens, String caseId, Event event) {
        return ccdDataApi.startEvent(
            tokens.getUserToken(), tokens.getS2sToken(), tokens.getUid(), JURISDICTION, CASE_TYPE,
            caseId, event.toString()
        );
    }

    private SubmitEventDetails submitEvent(
        Tokens tokens, String caseId, StartEventDetails startEventDetails, Event event, Map<String, Object> caseData) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", event.toString());
        CaseDataContent request =
            new CaseDataContent(caseId, caseData, eventData, startEventDetails.getToken(), true);

        return ccdDataApi.submitEvent(tokens.getUserToken(), tokens.getS2sToken(), caseId, request);

    }

    @Data
    private class Tokens {
        private String userToken;
        private String s2sToken;
        private String uid;
    }

}
