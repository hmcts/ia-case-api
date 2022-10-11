package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.commondata.CaseFlagDto;

@Service
@Slf4j
public class RdCommonDataClient {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private String commonDataUrl;
    private String commonDataApiCaseFlagPath;

    public RdCommonDataClient(RestTemplate restTemplate,
                                   AuthTokenGenerator serviceAuthTokenGenerator,
                                   @Value("${rd_commondata_api_url}") String commonDataUrl,
                                   @Value("${rd_commondata_api_case_flag_path}") String commonDataApiCaseFlagPath
    ) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.commonDataUrl = commonDataUrl;
        this.commonDataApiCaseFlagPath = commonDataApiCaseFlagPath;
    }

    public CaseFlagDto getStrategicCaseFlags() {

        //final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        //final String accessToken = userDetails.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        //headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        //headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<CaseFlagDto> response;
        try {
            response = restTemplate
                .exchange(
                    commonDataUrl + commonDataApiCaseFlagPath,
                    HttpMethod.GET,
                    requestEntity,
                    CaseFlagDto.class
                );

        } catch (RestClientResponseException e) {
            throw new CcdDataIntegrationException(
                "Error calling Rd-Common-Data-Api: " + commonDataUrl + commonDataApiCaseFlagPath,
                e
            );
        }

        log.info("Http status received from Rd-Common-Data-Api: {}", response.getStatusCodeValue());

        return response.getBody();

    }
}