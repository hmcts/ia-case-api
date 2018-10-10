package uk.gov.hmcts.reform.iacaseapi.shared.infrastructure.security;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.security.AccessTokenDecoder;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.security.AccessTokenProvider;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.security.UserAndRolesProvider;

@Service
public class AccessTokenUserAndRolesProvider implements UserAndRolesProvider {

    private final String USER_ID_KEY = "id";
    private final String ROLES_KEY = "data";

    private final AccessTokenProvider accessTokenProvider;
    private final AccessTokenDecoder accessTokenDecoder;

    public AccessTokenUserAndRolesProvider(
        @Autowired AccessTokenProvider accessTokenProvider,
        @Autowired AccessTokenDecoder accessTokenDecoder
    ) {
        this.accessTokenProvider = accessTokenProvider;
        this.accessTokenDecoder = accessTokenDecoder;
    }

    public String getUserId() {
        return tryGetUserId()
            .orElseThrow(() -> new IllegalStateException("User id not present"));
    }

    public Optional<String> tryGetUserId() {

        Optional<String> authorizationToken = accessTokenProvider.tryGetAccessToken();
        if (!authorizationToken.isPresent()) {
            return Optional.empty();
        }

        Map<String, String> accessTokenClaims = accessTokenDecoder.decode(authorizationToken.get());

        return Optional.of(
            accessTokenClaims.get(USER_ID_KEY)
        );
    }

    public List<String> getUserRoles() {

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
