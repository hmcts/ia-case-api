package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.idam.UserInfo;

@Component
public class IdamService {

    private final String systemUserName;
    private final String systemUserPass;
    private final String idamRedirectUrl;
    private final String systemUserScope;
    private final String idamClientId;
    private final String idamClientSecret;
    private final IdamApi idamApi;

    public IdamService(
        @Value("${idam.system.username}") String systemUserName,
        @Value("${idam.system.password}") String systemUserPass,
        @Value("${idam.redirectUrl}") String idamRedirectUrl,
        @Value("${idam.scope}") String systemUserScope,
        @Value("${spring.security.oauth2.client.registration.oidc.client-id}") String idamClientId,
        @Value("${spring.security.oauth2.client.registration.oidc.client-secret}") String idamClientSecret,
        IdamApi idamApi
    ) {
        this.systemUserName = systemUserName;
        this.systemUserPass = systemUserPass;
        this.idamRedirectUrl = idamRedirectUrl;
        this.systemUserScope = systemUserScope;
        this.idamClientId = idamClientId;
        this.idamClientSecret = idamClientSecret;
        this.idamApi = idamApi;
    }

    @Cacheable(value = "systemTokenCache")
    public Token getServiceUserToken() {
        Map<String, String> idamAuthDetails = new ConcurrentHashMap<>();

        idamAuthDetails.put("grant_type", "password");
        idamAuthDetails.put("redirect_uri", idamRedirectUrl);
        idamAuthDetails.put("client_id", idamClientId);
        idamAuthDetails.put("client_secret", idamClientSecret);
        idamAuthDetails.put("username", systemUserName);
        idamAuthDetails.put("password", systemUserPass);
        idamAuthDetails.put("scope", systemUserScope);

        return idamApi.token(idamAuthDetails);
    }

    @Cacheable(value = "userInfoCache")
    public UserInfo getUserInfo(String accessToken) {
        return idamApi.userInfo(accessToken);
    }
}
