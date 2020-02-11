package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.AccessTokenProvider;

@Slf4j
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

    public IdamUserDetails getUserDetails() {
        long startTime = System.currentTimeMillis();

        final String accessToken = accessTokenProvider.getAccessToken();

        log.info("Request for access token processed in {}ms", System.currentTimeMillis() - startTime);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, accessToken);

        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        Map<String, Object> response;

        try {

            startTime = System.currentTimeMillis();

            response =
                restTemplate
                    .exchange(
                        baseUrl + detailsUri,
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<Map<String, Object>>() {
                        }
                    ).getBody();

            log.info("Request for user details processed in {}ms", System.currentTimeMillis() - startTime);

        } catch (RestClientException ex) {

            throw new IdentityManagerResponseException(
                "Could not get user details with IDAM",
                ex
            );
        }

        if (response.get("id") == null) {
            throw new IllegalStateException("IDAM user details missing 'id' field");
        }

        if (response.get("roles") == null) {
            throw new IllegalStateException("IDAM user details missing 'roles' field");
        }

        if (response.get("email") == null) {
            throw new IllegalStateException("IDAM user details missing 'email' field");
        }

        if (response.get("forename") == null) {
            throw new IllegalStateException("IDAM user details missing 'forename' field");
        }

        if (response.get("surname") == null) {
            throw new IllegalStateException("IDAM user details missing 'surname' field");
        }

        return new IdamUserDetails(
            accessToken,
            String.valueOf(response.get("id")),
            castRolesToList(response.get("roles")),
            (String) response.get("email"),
            (String) response.get("forename"),
            (String) response.get("surname")
        );
    }

    @SuppressWarnings("unchecked")
    private List<String> castRolesToList(
        Object untypedRoles
    ) {
        List<String> roles = new ArrayList<>();

        if (untypedRoles instanceof List) {
            ((List) untypedRoles)
                .forEach(role -> roles.add((String) role));
        }

        return roles;
    }
}
