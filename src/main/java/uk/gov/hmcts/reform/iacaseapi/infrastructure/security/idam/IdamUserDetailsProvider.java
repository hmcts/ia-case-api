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

    @Override
    public IdamUserDetails getUserDetails(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, accessToken);

        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        Map<String, Object> response;

        try {

            response =
                restTemplate
                    .exchange(
                        baseUrl + detailsUri,
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<Map<String, Object>>() {
                        }
                    ).getBody();

        } catch (RestClientException ex) {

            throw new IdentityManagerResponseException(
                "Could not get user details with IDAM",
                ex
            );
        }

        if (response.get("uid") == null) {
            throw new IllegalStateException("IDAM user details missing 'uid' field");
        }

        if (response.get("roles") == null) {
            throw new IllegalStateException("IDAM user details missing 'roles' field");
        }

        if (response.get("sub") == null) {
            throw new IllegalStateException("IDAM user details missing 'sub' field");
        }

        if (response.get("given_name") == null) {
            throw new IllegalStateException("IDAM user details missing 'given_name' field");
        }

        if (response.get("family_name") == null) {
            throw new IllegalStateException("IDAM user details missing 'family_name' field");
        }

        return new IdamUserDetails(
            accessToken,
            String.valueOf(response.get("uid")),
            castRolesToList(response.get("roles")),
            (String) response.get("sub"),
            (String) response.get("given_name"),
            (String) response.get("family_name")
        );
    }

    public IdamUserDetails getUserDetails() {

        return getUserDetails(accessTokenProvider.getAccessToken());
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
