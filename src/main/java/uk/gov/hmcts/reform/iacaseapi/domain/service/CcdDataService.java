package uk.gov.hmcts.reform.iacaseapi.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdDataApi;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdentityManagerResponseException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
@Slf4j
public class CcdDataService {

    private final CcdDataApi ccdDataApi;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final IdamService idamService;
    private static final String CASE_TYPE = "Asylum";

    public CcdDataService(CcdDataApi ccdDataApi,
                          AuthTokenGenerator serviceAuthTokenGenerator,
                          IdamService idamService) {
        this.ccdDataApi = ccdDataApi;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.idamService = idamService;
    }

    public SubmitEventDetails updateLocalAuthorityPolicy(Callback<AsylumCase> callback) {

        String event = Event.RESET_REMOVE_LEGAL_REPRESENTATIVE.toString();
        CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        String jurisdiction = caseDetails.getJurisdiction();

        String userToken;
        String s2sToken;
        String uid;
        try {
            userToken = idamService.getServiceUserToken();
            log.info("System user token has been generated for event: {}, caseId: {}.", event, caseId);

            s2sToken = serviceAuthTokenGenerator.generate();
            log.info("S2S token has been generated for event: {}, caseId: {}.", event, caseId);

            uid = idamService.getUserInfo(userToken).getUid();
            //TODO: remove uid from logs
            log.info("System user id has been fetched for event: {}, caseId: {}, userID: {}", event, caseId, uid);

        } catch (IdentityManagerResponseException ex) {

            log.error("Unauthorized access to getCaseById {}", ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }

        // Get case details by Id
        final StartEventDetails startEventDetails = getCase(userToken, s2sToken, uid, jurisdiction, caseId);
        log.info("Case details found for the caseId: {}", caseId);

        AsylumCase asylumCase = startEventDetails.getCaseDetails().getCaseData();
        Map<String, Object> caseData = new HashMap<>();
        setLocalAuthorityPolicy(asylumCase, caseData, caseId);
        caseData.put("organisationPolicyToRemove", null);
        caseData.put("changeOrganisationRequestField", null);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", CASE_TYPE);

        SubmitEventDetails submitEventDetails = submitEvent(userToken, s2sToken, caseId, caseData, eventData,
                startEventDetails.getToken());

        log.info("Change Organisation Request Field reset for the caseId: {}, Status: {}, Message: {}", caseId,
                submitEventDetails.getCallbackResponseStatusCode(), submitEventDetails.getCallbackResponseStatus());

        return submitEventDetails;
    }

    private void setLocalAuthorityPolicy(AsylumCase asylumCase, Map<String, Object> caseData, String caseId) {
        Optional<OrganisationPolicy> organisationPolicyToRemove = asylumCase.read(
                AsylumCaseFieldDefinition.ORGANISATION_POLICY_TO_REMOVE, OrganisationPolicy.class);

        boolean isLocalAuthorityPolicyMissing = isLocalAuthorityPolicyMissing(asylumCase);
        if (isLocalAuthorityPolicyMissing
                && !(organisationPolicyToRemove.isEmpty()
                || isEmpty(organisationPolicyToRemove.get().getOrganisation())
                || isEmpty(organisationPolicyToRemove.get().getOrganisation().getOrganisationID()))) {

            caseData.put("localAuthorityPolicy", organisationPolicyToRemove.get());
        } else {
            log.info("Skipping resetting Legal representative's OrganisationPolicy for the caseId: " + caseId);
        }
    }

    private StartEventDetails getCase(
        String userToken, String s2sToken, String uid, String jurisdiction, String caseId) {

        return ccdDataApi.startEvent(userToken, s2sToken, uid, jurisdiction, CASE_TYPE,
                                     caseId, Event.UPDATE_PAYMENT_STATUS.toString());
    }

    private SubmitEventDetails submitEvent(
        String userToken, String s2sToken, String caseId, Map<String, Object> data,
        Map<String, Object> eventData, String eventToken) {
        CaseDataContent request = new CaseDataContent(caseId, data, eventData, eventToken, true);
        return ccdDataApi.submitEvent(userToken, s2sToken, caseId, request);
    }

    private boolean isLocalAuthorityPolicyMissing(AsylumCase asylumCase) {

        Optional<OrganisationPolicy> localAuthorityPolicy = asylumCase.read(
                AsylumCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class);

        return localAuthorityPolicy.isEmpty()
                || isEmpty(localAuthorityPolicy.get().getOrganisation())
                || isEmpty(localAuthorityPolicy.get().getOrganisation().getOrganisationID());
    }

}
