package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdamAuthorizor;

@Service
@Qualifier("systemUser")
public class SystemUserAccessTokenProvider implements AccessTokenProvider {

    private final String iaSystemUser;
    private final String iaSystemPassword;
    private final IdamAuthorizor idamAuthorizor;

    public SystemUserAccessTokenProvider(
        @Value("${ia_system_user}") String iaSystemUser,
        @Value("${ia_system_user_password}") String iaSystemPassword,
        IdamAuthorizor idamAuthorizor
    ) {
        this.iaSystemUser = iaSystemUser;
        this.iaSystemPassword = iaSystemPassword;
        this.idamAuthorizor = idamAuthorizor;
    }

    public String getAccessToken() {
        return tryGetAccessToken()
            .orElseThrow(() -> new IllegalStateException("Access token not present"));
    }

    public Optional<String> tryGetAccessToken() {

        return Optional.ofNullable(
            idamAuthorizor.exchangeForAccessToken(
                iaSystemUser,
                iaSystemPassword
            )
        );
    }
}
