package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ref.OrganisationEntityResponse;

@Slf4j
@Service
public class ProfessionalOrganisationRetriever {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserDetailsProvider userDetailsProvider;
    private final String refDataApiUrl;
    private final String refDataApiPath;

    public ProfessionalOrganisationRetriever(RestTemplate restTemplate,
                                             AuthTokenGenerator serviceAuthTokenGenerator,
                                             UserDetailsProvider userDetailsProvider,
                                             @Value("${prof.ref.data.url}") String refDataApiUrl,
                                             @Value("${prof.ref.data.path.org.organisation}") String refDataApiPath) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.userDetailsProvider = userDetailsProvider;
        this.refDataApiUrl = refDataApiUrl;
        this.refDataApiPath = refDataApiPath;
    }

    public OrganisationEntityResponse retrieve() {

        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        OrganisationEntityResponse response = null;

        log.info("Calling Ref Data endpoint: {}", refDataApiUrl + refDataApiPath);

        try {
            response =
                restTemplate
                    .exchange(
                        refDataApiUrl + refDataApiPath,
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<OrganisationEntityResponse>() {
                        }
                    ).getBody();

        } catch (Throwable ex) {
            log.warn("Cannot fetch Professional Reference data, exception message: " + ex.getMessage(), ex);
        }

        log.info("Response returned: {} userId[{}]", String.valueOf(response), userDetails.getId());

        return response;
    }
}
