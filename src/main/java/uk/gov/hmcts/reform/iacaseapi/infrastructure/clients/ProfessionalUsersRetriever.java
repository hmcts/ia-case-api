package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;

@Slf4j
@Service
public class ProfessionalUsersRetriever {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserDetails userDetails;
    private final String refDataApiUrl;
    private final String refDataApiPath;

    public ProfessionalUsersRetriever(RestTemplate restTemplate,
                                      AuthTokenGenerator serviceAuthTokenGenerator,
                                      UserDetails userDetails,
                                      @Value("${prof.ref.data.url}") String refDataApiUrl,
                                      @Value("${prof.ref.data.path.org.users}") String refDataApiPath) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.userDetails = userDetails;
        this.refDataApiUrl = refDataApiUrl;
        this.refDataApiPath = refDataApiPath;
    }

    public ProfessionalUsersResponse retrieve() {

        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final String accessToken = userDetails.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ProfessionalUsersResponse response;

        log.info("Calling Ref Data endpoint: {}", refDataApiUrl + refDataApiPath);
        log.info(refDataApiUrl);
        try {
            response =
                restTemplate
                    .exchange(
                        refDataApiUrl + refDataApiPath,
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<ProfessionalUsersResponse>() {
                        }
                    ).getBody();

        } catch (RestClientResponseException ex) {
            log.info(String.valueOf(ex));
            throw new ReferenceDataIntegrationException(
                "Couldn't retrieve organisations using API: " + refDataApiUrl + refDataApiPath,
                ex
            );
        }

        log.info("response returned: {}", response);

        return response;


    }
}
