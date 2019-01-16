package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.AccessTokenProvider;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

@Service
public class IdamUserDetailsProvider implements UserDetailsProvider {

    private final AccessTokenProvider accessTokenProvider;
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String detailsUri;

    public IdamUserDetailsProvider(
        AccessTokenProvider accessTokenProvider,
        RestTemplate restTemplate,
        @Value("${auth.idam.client.baseUrl}") String baseUrl,
        @Value("${auth.idam.client.detailsUri}") String detailsUri
    ) {
        requireNonNull(baseUrl);
        requireNonNull(detailsUri);

        this.accessTokenProvider = accessTokenProvider;
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.detailsUri = detailsUri;
    }

    public UserDetails getUserDetails() {

        String accessToken = accessTokenProvider.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, accessToken);

        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        Map<String, Object> response;

        try {

            response = restTemplate
                .exchange(
                    baseUrl + detailsUri,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }
                ).getBody();

        } catch (RestClientException ex) {
            throw new IdentityManagerResponseException(AlertLevel.P2,
                "Could not get user details with IDAM",
                ex
            );
        }

        if (response.get("forename") == null) {
            throw new IllegalStateException("IDAM user details missing 'forename' field");
        }

        if (response.get("surname") == null) {
            throw new IllegalStateException("IDAM user details missing 'surname' field");
        }

        if (response.get("email") == null) {
            throw new IllegalStateException("IDAM user details missing 'email' field");
        }

        return new UserDetails(
            (String) response.get("forename"),
            (String) response.get("surname"),
            (String) response.get("email")
        );
    }
}
