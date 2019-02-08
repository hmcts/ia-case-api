package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import java.util.Base64;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

@Service
public class IdamAuthorizor {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String clientId;
    private final String clientSecret;
    private final String clientRedirectUri;

    public IdamAuthorizor(
        RestTemplate restTemplate,
        @Value("${auth.idam.client.baseUrl}") String baseUrl,
        @Value("${auth.idam.client.id}") String clientId,
        @Value("${auth.idam.client.secret}") String clientSecret,
        @Value("${auth.idam.client.redirectUri}") String clientRedirectUri
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientRedirectUri = clientRedirectUri;
    }

    public String exchangeForAccessToken(
        @NotNull String username,
        @NotNull String password
    ) {
        String basicAuthorization =
            "Basic "
            + Base64
                .getEncoder()
                .encodeToString(
                    (username + ":" + password).getBytes()
                );

        return "Bearer " + fetchTokenAuthorization(fetchCodeAuthorization(basicAuthorization));
    }

    private String fetchCodeAuthorization(
        String basicAuthorization
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(HttpHeaders.AUTHORIZATION, basicAuthorization);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("response_type", "code");
        body.add("client_id", clientId);
        body.add("redirect_uri", clientRedirectUri);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        Map<String, String> response;

        try {

            response =
                restTemplate
                    .exchange(
                        baseUrl + "/oauth2/authorize",
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<Map<String, String>>() {
                        }
                    ).getBody();

        } catch (RestClientException e) {

            throw new IdentityManagerResponseException(
                AlertLevel.P2,
                "Could not get auth code with IDAM",
                e
            );
        }

        return response.getOrDefault("code", "");
    }

    private String fetchTokenAuthorization(
        String code
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", clientRedirectUri);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        Map<String, String> response;

        try {

            response =
                restTemplate
                    .exchange(
                        baseUrl + "/oauth2/token",
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<Map<String, String>>() {
                        }
                    ).getBody();

        } catch (RestClientException e) {

            throw new IdentityManagerResponseException(
                AlertLevel.P2,
                "Could not get auth token with IDAM",
                e
            );
        }

        return response.getOrDefault("access_token", "");
    }
}
