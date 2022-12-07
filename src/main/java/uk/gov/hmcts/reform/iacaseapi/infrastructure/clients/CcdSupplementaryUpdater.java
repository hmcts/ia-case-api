package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Maps;
import java.net.URI;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Service
@Slf4j
public class CcdSupplementaryUpdater {
    public static final String HMCTS_SERVICE_ID = "HMCTSServiceId";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final FeatureToggler featureToggler;
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserDetails userDetails;
    private String ccdUrl;
    private String ccdSupplementaryApiPath;
    private String hmctsServiceId;

    public CcdSupplementaryUpdater(FeatureToggler featureToggler, RestTemplate restTemplate,
                                   AuthTokenGenerator serviceAuthTokenGenerator,
                                   UserDetails userDetails,
                                   @Value("${core_case_data_api_url}") String ccrUrl,
                                   @Value("${core_case_data_api_supplementary_data_path}") String ccdSupplementaryApiPath,
                                   @Value("${hmcts_service_id}") String hmctsServiceId
    ) {
        this.featureToggler = featureToggler;
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.userDetails = userDetails;
        this.ccdUrl = ccrUrl;
        this.ccdSupplementaryApiPath = ccdSupplementaryApiPath;
        this.hmctsServiceId = hmctsServiceId;
    }

    public void setHmctsServiceIdSupplementary(final Callback<AsylumCase> callback) {
        if (featureToggler.getValue("wa-R3-feature", false)) {
            requireNonNull(callback, "callback must not be null");

            final long caseId = callback.getCaseDetails().getId();

            final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
            final String accessToken = userDetails.getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.set(HttpHeaders.AUTHORIZATION, accessToken);
            headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);

            Map<String, Map<String, Object>> payloadData = Maps.newHashMap();
            payloadData.put("$set", singletonMap(HMCTS_SERVICE_ID, hmctsServiceId));

            Map<String, Object> payload = Maps.newHashMap();
            payload.put("supplementary_data_updates", payloadData);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            URI uri = UriComponentsBuilder
                    .fromPath(ccdSupplementaryApiPath)
                    .build(caseId);

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
                        "Couldn't update CCD case supplementary data using API: " + ccdUrl + ccdSupplementaryApiPath,
                        e
                );
            }

            log.info("Http status received from CCD supplementary update API; {}", response.getStatusCodeValue());
        }
    }
}