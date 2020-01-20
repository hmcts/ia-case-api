package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

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

        return "Bearer " + fetchTokenAuthorization(username, password);
    }

    private String fetchTokenAuthorization(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("username", username);
        body.add("password", password);
        body.add("grant_type", "password");
        body.add("redirect_uri", clientRedirectUri);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("scope", "openid profile roles");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        Map<String, String> response;

        try {

            response =
                restTemplate
                    .exchange(
                        baseUrl + "/o/token",
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<Map<String, String>>() {
                        }
                    ).getBody();

        } catch (RestClientException e) {

            throw new IdentityManagerResponseException(
                "Could not get auth token with IDAM",
                e
            );
        }

        return response.getOrDefault("access_token", "");
    }
}
