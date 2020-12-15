package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Maps;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ProfessionalUser;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;


@Slf4j
@Service
public class CcdCaseAssignment {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserDetailsProvider userDetailsProvider;
    private final ProfessionalUsersRetriever professionalUsersRetriever;
    private final String ccdUrl;
    private final String ccdAssignmentsApiPath;
    private final String ccdPermissionsRevokeApiPath;

    public CcdCaseAssignment(RestTemplate restTemplate,
                             AuthTokenGenerator serviceAuthTokenGenerator,
                             UserDetailsProvider userDetailsProvider,
                             ProfessionalUsersRetriever professionalUsersRetriever,
                             @Value("${core_case_data_api_assignments_url}") String ccdUrl,
                             @Value("${core_case_data_api_assignments_path}") String ccdAssignmentsApiPath,
                             @Value("${core_case_data_api_permissions_revoke_path}") String ccdPermissionsRevokeApiPath
    ) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.userDetailsProvider = userDetailsProvider;
        this.professionalUsersRetriever = professionalUsersRetriever;
        this.ccdUrl = ccdUrl;
        this.ccdAssignmentsApiPath = ccdAssignmentsApiPath;
        this.ccdPermissionsRevokeApiPath = ccdPermissionsRevokeApiPath;
    }

    public void revokeAccessToCase(
        final Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        final long caseId = callback.getCaseDetails().getId();
        final String jurisdiction = callback.getCaseDetails().getJurisdiction();
        final String caseTypeId = "Asylum";
        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();
        final String idamUserId = userDetails.getId();

        setHeaders(serviceAuthorizationToken, accessToken);

        URI uri = UriComponentsBuilder
            .fromPath(ccdPermissionsRevokeApiPath)
            //.build(idamUserId, jurisdiction, caseTypeId, caseId, idamUserId);
            .build("835c71bd-1f73-47c1-a00d-453f954d0d56", "IA", "Asylum", "1606907972859898", "835c71bd-1f73-47c1-a00d-453f954d0d56");

        HttpEntity<Map<String, Object>> requestEntity =
            new HttpEntity<>(setHeaders(serviceAuthorizationToken, accessToken));

        ResponseEntity<Object> response;
        try {
            response = restTemplate
                .exchange(
                    ccdUrl + uri.getPath(),
                    HttpMethod.DELETE,
                    requestEntity,
                    Object.class
                );

        } catch (RestClientResponseException e) {
            throw new CcdDataIntegrationException(
                "Couldn't revoke CCD case access using API: " + ccdUrl + uri.getPath(),
                e
            );
        }

        log.info("Http status received from CCD API; {}. Case access revoked", response.getStatusCodeValue());
    }

    public void assignAccessToCase(
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

        Map<String, Object> caseUser = Maps.newHashMap();
        caseUser.put("case_id", "1606907972859898");
        caseUser.put("case_role", "[caseworker-ia-legalrep-solicitor]");
        caseUser.put("organisation_id", "ZE2KIWO");
        caseUser.put("user_id", "835c71bd-1f73-47c1-a00d-453f954d0d56");

        ArrayList<Map<String, Object>> caseUsers = new ArrayList<>();
        caseUsers.add(caseUser);

        Map<String, Object> payload = Maps.newHashMap();
        payload.put("case_users", caseUsers);

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
                    HttpMethod.POST,
                    requestEntity,
                    Object.class
                );

        } catch (RestClientResponseException e) {
            throw new CcdDataIntegrationException(
                "Couldn't set initial CCD case assignment using API: " + ccdUrl + ccdAssignmentsApiPath,
                e
            );
        }

        log.info("Http status received from CCD API; {}", response.getStatusCodeValue());
    }

    public List<ProfessionalUser> getOrganisationUsers(final String organisationIdentifier) {

        List<Map<String, Object>> caseUsers =
            professionalUsersRetriever.retrieve().getUsers()
                .stream()
                .map(user -> {
                    Map<String, Object> caseUser = Maps.newHashMap();
                    caseUser.put("case_id", "1606907972859898");
                    caseUser.put("case_role", "caseworker-ia-legalrep-solicitor");
                    caseUser.put("organisation_id", organisationIdentifier);
                    caseUser.put("user_id", user.getUserIdentifier());
                    return caseUser;
                })
                .collect(Collectors.toList());

        Map<String, Object> payload = Maps.newHashMap();
        payload.put("case_users", caseUsers);

        return professionalUsersRetriever.retrieve().getUsers();

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
