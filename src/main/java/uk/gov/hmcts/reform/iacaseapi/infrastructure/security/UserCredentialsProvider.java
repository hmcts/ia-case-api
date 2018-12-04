package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class UserCredentialsProvider implements AccessTokenProvider {

    private static final String USER_ID_KEY = "id";
    private static final String ROLES_KEY = "data";

    private final AccessTokenProvider accessTokenProvider;
    private final AccessTokenDecoder accessTokenDecoder;

    public UserCredentialsProvider(
        AccessTokenProvider accessTokenProvider,
        AccessTokenDecoder accessTokenDecoder
    ) {
        this.accessTokenProvider = accessTokenProvider;
        this.accessTokenDecoder = accessTokenDecoder;
    }

    public String getAccessToken() {
        return accessTokenProvider.getAccessToken();
    }

    public Optional<String> tryGetAccessToken() {
        return accessTokenProvider.tryGetAccessToken();
    }

    public String getId() {
        return tryGetId()
            .orElseThrow(() -> new IllegalStateException("User id not present"));
    }

    public Optional<String> tryGetId() {

        Optional<String> authorizationToken = accessTokenProvider.tryGetAccessToken();
        if (!authorizationToken.isPresent()) {
            return Optional.empty();
        }

        Map<String, String> accessTokenClaims = accessTokenDecoder.decode(authorizationToken.get());

        return Optional.of(
            accessTokenClaims.get(USER_ID_KEY)
        );
    }

    public List<String> getRoles() {

        Optional<String> authorizationToken = accessTokenProvider.tryGetAccessToken();
        if (!authorizationToken.isPresent()) {
            return Collections.emptyList();
        }

        Map<String, String> accessTokenClaims = accessTokenDecoder.decode(authorizationToken.get());

        return Arrays.asList(
            accessTokenClaims
                .get(ROLES_KEY)
                .replace("\"", "")
                .split(",")
        );
    }
}
