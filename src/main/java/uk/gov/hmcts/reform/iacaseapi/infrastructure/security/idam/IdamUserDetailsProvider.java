package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static java.util.Objects.requireNonNull;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
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

        String accessToken = accessTokenProvider.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, accessToken);

        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        try {

            Map<String, Object> response = Optional
                .of(restTemplate
                    .exchange(
                        baseUrl + detailsUri,
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<Map<String, Object>>() {
                        }
                    )
                )
                .map(ResponseEntity::getBody)
                .orElse(new HashMap<>());

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

        } catch (RestClientResponseException ex) {

            throw new IdentityManagerResponseException(
                "Could not get user details with IDAM",
                ex
            );
        } catch (IllegalStateException e) {

            throw new IdentityManagerResponseException(
                e.getMessage(),
                e
            );
        }
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
