package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Maps;
import java.net.URI;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;

@Service
@Slf4j
public class CcdSupplementaryUpdater {
    public static final String HMCTS_SERVICE_ID = "HMCTSServiceId";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserDetails userDetails;
    private final String ccdUrl;
    private final String ccdSupplementaryApiPath;
    private final String hmctsServiceId;

    public CcdSupplementaryUpdater(RestTemplate restTemplate,
                                   AuthTokenGenerator serviceAuthTokenGenerator,
                                   UserDetails userDetails,
                                   @Value("${core_case_data_api_url}") String ccrUrl,
                                   @Value("${core_case_data_api_supplementary_data_path}")
                                       String ccdSupplementaryApiPath,
                                   @Value("${hmcts_service_id}") String hmctsServiceId
    ) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.userDetails = userDetails;
        this.ccdUrl = ccrUrl;
        this.ccdSupplementaryApiPath = ccdSupplementaryApiPath;
        this.hmctsServiceId = hmctsServiceId;
    }

    public void setHmctsServiceIdSupplementary(
        final Callback<BailCase> callback
    ) {
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
        String url = ccdUrl + uri.getPath();
        try {
            response = restTemplate
                .exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Object.class
                );
            log.info("Http status received from CCD supplementary update API; {}", response.getStatusCodeValue());
        } catch (RestClientResponseException e) {
            log.info("Couldn't update CCD case supplementary data using API: [{}]", url, e);
        }
    }
}
