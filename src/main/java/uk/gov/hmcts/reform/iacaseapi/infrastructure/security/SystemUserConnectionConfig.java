package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SystemUserConnectionConfig implements IdamUserConnectionConfig {

    private final String iaSystemUser;
    private final String iaSystemPassword;
    private final IdamAuthorizor idamAuthorizor;
    private final AccessTokenDecoder accessTokenDecoder;

    public SystemUserConnectionConfig(
            @Value("${ia_system_user}") String iaUser,
            @Value("${ia_system_user_password}") String iaUserPassword,
            IdamAuthorizor idamAuthorizor,
            AccessTokenDecoder accessTokenDecoder
    ) {
        this.iaSystemUser = iaUser;
        this.iaSystemPassword = iaUserPassword;
        this.idamAuthorizor = idamAuthorizor;
        this.accessTokenDecoder = accessTokenDecoder;
    }

    @Override
    public String getAccessToken() {
        return idamAuthorizor.exchangeForAccessToken(
                iaSystemUser,
                iaSystemPassword);
    }

    @Override
    public String getId() {
        return accessTokenDecoder.decode(getAccessToken()).get("id");
    }
}
