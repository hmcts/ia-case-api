package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CcdDataService;


@Slf4j
@Service
public class CcdCaseAssignment {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserDetailsProvider userDetailsProvider;
    private final CcdDataService ccdDataService;
    private final String ccdUrl;
    private final String aacUrl;
    private final String ccdAssignmentsApiPath;
    private final String aacAssignmentsApiPath;
    private final String applyNocAssignmentsApiPath;

    public CcdCaseAssignment(RestTemplate restTemplate,
                             AuthTokenGenerator serviceAuthTokenGenerator,
                             UserDetailsProvider userDetailsProvider,
                             CcdDataService ccdDataService,
                             @Value("${core_case_data_api_assignments_url}") String ccdUrl,
                             @Value("${assign_case_access_api_url}") String aacUrl,
                             @Value("${core_case_data_api_assignments_path}") String ccdAssignmentsApiPath,
                             @Value("${assign_case_access_api_assignments_path}") String aacAssignmentsApiPath,
                             @Value("${apply_noc_access_api_assignments_path}") String applyNocAssignmentsApiPath
    ) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.userDetailsProvider = userDetailsProvider;
        this.ccdDataService = ccdDataService;
        this.ccdUrl = ccdUrl;
        this.aacUrl = aacUrl;
        this.ccdAssignmentsApiPath = ccdAssignmentsApiPath;
        this.aacAssignmentsApiPath = aacAssignmentsApiPath;
        this.applyNocAssignmentsApiPath = applyNocAssignmentsApiPath;
    }

    public void revokeAccessToCase(
        final Callback<AsylumCase> callback,
        final String organisationIdentifier
    ) {
        requireNonNull(callback, "callback must not be null");
        requireNonNull(organisationIdentifier, "organisation identifier must not be null");

        final long caseId = callback.getCaseDetails().getId();
        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();
        final String idamUserId = userDetails.getId();

        Map<String, Object> payload = buildRevokeAccessPayload(organisationIdentifier, caseId, idamUserId);

        HttpEntity<Map<String, Object>> requestEntity =
            new HttpEntity<>(
                payload,
                setHeaders(serviceAuthorizationToken, accessToken)
            );

        ResponseEntity<Object> response;
        try {
            response = restTemplate
                .exchange(
                    ccdUrl + ccdAssignmentsApiPath,
                    HttpMethod.DELETE,
                    requestEntity,
                    Object.class
                );

        } catch (RestClientResponseException e) {
            throw new CcdDataIntegrationException(
                "Couldn't revoke CCD case access for case ["
                + callback.getCaseDetails().getId()
                + "] using API: "
                + ccdUrl + ccdAssignmentsApiPath,
                e
            );
        }

        log.info("Revoke Access. Http status received from CCD API; {} for case {}",
            response.getStatusCodeValue(), callback.getCaseDetails().getId());
    }

    public void assignAccessToCase(
        final Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        final long caseId = callback.getCaseDetails().getId();
        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();
        final String idamUserId = userDetails.getId();

        Map<String, Object> payload = buildAssignAccessCaseUserMap(caseId, idamUserId);

        HttpEntity<Map<String, Object>> requestEntity =
            new HttpEntity<>(
                payload,
                setHeaders(serviceAuthorizationToken, accessToken)
            );

        ResponseEntity<Object> response;
        try {
            response = restTemplate
                .exchange(
                    aacUrl + aacAssignmentsApiPath,
                    HttpMethod.POST,
                    requestEntity,
                    Object.class
                );

        } catch (RestClientResponseException e) {
            throw new CcdDataIntegrationException(
                "Couldn't set initial AAC case assignment for case ["
                + callback.getCaseDetails().getId()
                + "] using API: "
                + aacUrl + aacAssignmentsApiPath,
                e
            );
        }

        log.info("Assign Access. Http status received from AAC API; {} for case {}",
            response.getStatusCodeValue(), callback.getCaseDetails().getId());
    }

    public void applyNoc(
        final Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();

        HttpEntity<Callback<AsylumCase>> requestEntity =
            new HttpEntity<>(
                callback,
                setHeaders(serviceAuthorizationToken, accessToken)
            );

        ResponseEntity<Object> response;
        try {
            response = restTemplate
                .exchange(
                    aacUrl + applyNocAssignmentsApiPath,
                    HttpMethod.POST,
                    requestEntity,
                    Object.class
                );
            
        } catch (RestClientResponseException e) {
            if (callback.getEvent() == Event.REMOVE_REPRESENTATION
                    || callback.getEvent() == Event.REMOVE_LEGAL_REPRESENTATIVE) {

                log.error("Removing Legal Representative failed for caseId {}. "
                        + "Resetting ChangeOrganisationRequestField value.",
                        callback.getCaseDetails().getId());
                ccdDataService.updateLocalAuthorityPolicy(callback);
            }

            throw new CcdDataIntegrationException(
                "Couldn't apply noc AAC case assignment for case ["
                + callback.getCaseDetails().getId()
                + "] using API: "
                + aacUrl + applyNocAssignmentsApiPath,
                e
            );
        }

        log.info("Apply NoC. Http status received from AAC API; {} for case {}",
            response.getStatusCodeValue(), callback.getCaseDetails().getId());
    }

    public Map<String, Object> buildRevokeAccessPayload(String organisationIdentifier, long caseId, String idamUserId) {
        Map<String, Object> caseUser = buildRevokeAccessCaseUserMap(organisationIdentifier, caseId, idamUserId);

        ArrayList<Map<String, Object>> caseUsers = new ArrayList<>();
        caseUsers.add(caseUser);

        Map<String, Object> payload = Maps.newHashMap();
        payload.put("case_users", caseUsers);
        return payload;
    }

    public Map<String, Object> buildAssignAccessCaseUserMap(long caseId, String idamUserId) {
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("case_id", caseId);
        payload.put("assignee_id", idamUserId);
        payload.put("case_type_id", "Asylum");
        return payload;
    }

    private Map<String, Object> buildRevokeAccessCaseUserMap(String organisationIdentifier, long caseId, String idamUserId) {
        Map<String, Object> caseUser = Maps.newHashMap();
        caseUser.put("case_id", caseId);
        caseUser.put("case_role", "[CREATOR]");
        caseUser.put("organisation_id", organisationIdentifier);
        caseUser.put("user_id", idamUserId);
        return caseUser;
    }

    private HttpHeaders setHeaders(String serviceAuthorizationToken, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);
        return headers;
    }
}
