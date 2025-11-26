package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.CcdSearchResult;

/**
 * Repository for performing Elasticsearch queries against CCD.
 * This component provides low-level access to CCD's Elasticsearch API.
 */
@Slf4j
@Repository
public class CcdElasticSearchRepository {

    private static final String SEARCH_CASES_ENDPOINT = "/searchCases";
    private static final String CASE_TYPE_PARAM = "?ctid=Asylum";

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserDetails userDetails;
    private final String ccdUrl;

    public CcdElasticSearchRepository(
        RestTemplate restTemplate,
        AuthTokenGenerator serviceAuthTokenGenerator,
        UserDetails userDetails,
        @Value("${core_case_data_api_url}") String ccdUrl
    ) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.userDetails = userDetails;
        this.ccdUrl = ccdUrl;
    }

    /**
     * Executes an Elasticsearch query against CCD.
     *
     * @param query The Elasticsearch query to execute
     * @return CcdSearchResult containing matching cases
     * @throws CcdSearchException if the search fails
     */
    public CcdSearchResult searchCases(CcdSearchQuery query) {
        try {
            String url = ccdUrl + SEARCH_CASES_ENDPOINT + CASE_TYPE_PARAM;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(AUTHORIZATION, userDetails.getAccessToken());
            headers.set(SERVICE_AUTHORIZATION, serviceAuthTokenGenerator.generate());

            HttpEntity<CcdSearchQuery> requestEntity = new HttpEntity<>(query, headers);

            log.info("Executing Elasticsearch query against CCD: {}", url);

            ResponseEntity<CcdSearchResult> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                CcdSearchResult.class
            );

            CcdSearchResult result = response.getBody();
            
            if (result != null) {
                log.info("Elasticsearch query returned {} results", result.getTotal());
            }

            return result;

        } catch (RestClientException e) {
            log.error("Error executing Elasticsearch query against CCD", e);
            throw new CcdSearchException("Failed to search cases in CCD", e);
        }
    }

    /**
     * Exception thrown when CCD search fails.
     */
    public static class CcdSearchException extends RuntimeException {
        public CcdSearchException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

