package uk.gov.hmcts.reform.iacaseapi.domain.service.ccddataservice;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdDataApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdentityManagerResponseException;

@Service
@Slf4j
public class CcdDataService {

    protected final CcdDataApi ccdDataApi;
    protected final IdamService idamService;
    protected final AuthTokenGenerator serviceAuthorization;
    protected static final String JURISDICTION = "IA";
    protected static final String CASE_TYPE = "Asylum";
    protected String userToken;
    protected String s2sToken;
    protected String uid;

    public CcdDataService(CcdDataApi ccdDataApi,
                          IdamService idamService,
                          AuthTokenGenerator serviceAuthorization) {

        this.ccdDataApi = ccdDataApi;
        this.idamService = idamService;
        this.serviceAuthorization = serviceAuthorization;
    }

    protected void authorize(Event eventToAuthorize, String caseId) {
        String event = eventToAuthorize.toString();

        try {
            userToken = idamService.getServiceUserToken();
            log.info("System user token has been generated for event: {}, caseId: {}.", event, caseId);

            s2sToken = serviceAuthorization.generate();
            log.info("S2S token has been generated for event: {}, caseId: {}.", event, caseId);

            uid = idamService.getSystemUserId(userToken);
            log.info("System user id has been fetched for event: {}, caseId: {}.", event, caseId);

        } catch (IdentityManagerResponseException ex) {

            log.error("CcdDataService failed to be authorized: {}", ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }
    }

    protected SubmitEventDetails submitEvent(
        String userToken,
        String s2sToken,
        String caseId,
        Map<String, Object> data,
        Map<String, Object> eventData,
        String eventToken) {

        CaseDataContent request =
            new CaseDataContent(caseId, data, eventData, eventToken, true);

        return ccdDataApi.submitEvent(userToken, s2sToken, caseId, request);
    }

    protected StartEventDetails startEvent(
        String userToken,
        String s2sToken,
        String uid,
        String caseType,
        String caseId,
        Event event) {
        return ccdDataApi.startEvent(userToken, s2sToken, uid, CcdDataService.JURISDICTION, caseType,
            caseId, event.toString());
    }

}

