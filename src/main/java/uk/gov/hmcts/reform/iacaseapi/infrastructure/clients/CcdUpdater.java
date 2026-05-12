package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Maps;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@Service
@Slf4j
public class CcdUpdater {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserDetails userDetails;
    private String ccdUrl;
    private String ccdPermissionsApiPath;

    public CcdUpdater(RestTemplate restTemplate,
                      AuthTokenGenerator serviceAuthTokenGenerator,
                      UserDetails userDetails,
                      @Value("${core_case_data_api_url}") String ccrUrl,
                      @Value("${core_case_data_api_permissions_path}") String ccdPermissionsApiPath
    ) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.userDetails = userDetails;
        this.ccdUrl = ccrUrl;
        this.ccdPermissionsApiPath = ccdPermissionsApiPath;
    }

    public void updatePermissions(
        final Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final long caseId = callback.getCaseDetails().getId();
        final String jurisdiction = callback.getCaseDetails().getJurisdiction();
        final String caseTypeId = "Asylum";

        Optional<DynamicList> maybeDynamicList = asylumCase.read(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS);
        DynamicList dynamicList = maybeDynamicList
            .orElseThrow(() -> new IllegalStateException(
                AsylumCaseFieldDefinition.ORG_LIST_OF_USERS + " is empty in case data when required.")
            );
        final uk.gov.hmcts.reform.iacaseapi.domain.entities.Value selectedValue = dynamicList.getValue();

        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final String accessToken = userDetails.getAccessToken();
        final String idamUserId = userDetails.getId();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);

        Map<String, Object> payload = Maps.newHashMap();
        payload.put("id", selectedValue.getCode());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        URI uri = UriComponentsBuilder
            .fromPath(ccdPermissionsApiPath)
            .build(idamUserId, jurisdiction, caseTypeId, caseId);

        ResponseEntity<Object> response;
        try {
            response = restTemplate
                .exchange(
                    ccdUrl + uri.getPath(),
                    HttpMethod.POST,
                    requestEntity,
                    Object.class
                );

        } catch (RestClientResponseException e) {
            throw new CcdDataIntegrationException(
                "Couldn't update CCD case permissions using API: " + ccdUrl + ccdPermissionsApiPath,
                e
            );
        }

        log.info("Http status received from CCD API; {}", response.getStatusCode().value());

    }
}