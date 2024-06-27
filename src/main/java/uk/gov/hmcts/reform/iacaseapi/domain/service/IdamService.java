package uk.gov.hmcts.reform.iacaseapi.domain.service;

import feign.FeignException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdentityManagerResponseException;

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
        @Value("${idam.ia_system_user.username}") String systemUserName,
        @Value("${idam.ia_system_user.password}") String systemUserPass,
        @Value("${idam.redirectUrl}") String idamRedirectUrl,
        @Value("${idam.ia_system_user.scope}") String scope,
        @Value("${spring.security.oauth2.client.registration.oidc.client-id}") String idamClientId,
        @Value("${spring.security.oauth2.client.registration.oidc.client-secret}") String idamClientSecret,
        IdamApi idamApi
    ) {
        this.systemUserName = systemUserName;
        this.systemUserPass = systemUserPass;
        this.idamRedirectUrl = idamRedirectUrl;
        this.systemUserScope = scope;
        this.idamClientId = idamClientId;
        this.idamClientSecret = idamClientSecret;
        this.idamApi = idamApi;
    }

    @Cacheable(value = "accessTokenCache")
    public String getServiceUserToken() {
        Map<String, String> idamAuthDetails = new ConcurrentHashMap<>();

        idamAuthDetails.put("grant_type", "password");
        idamAuthDetails.put("redirect_uri", idamRedirectUrl);
        idamAuthDetails.put("client_id", idamClientId);
        idamAuthDetails.put("client_secret", idamClientSecret);
        idamAuthDetails.put("username", systemUserName);
        idamAuthDetails.put("password", systemUserPass);
        idamAuthDetails.put("scope", systemUserScope);

        return "Bearer " + idamApi.token(idamAuthDetails).getAccessToken();
    }

    @Cacheable(value = "userInfoCache")
    public UserInfo getUserInfo(String accessToken) {
        return idamApi.userInfo(accessToken);
    }

    public String getSystemUserId(String userToken) {
        try {
            return idamApi.userInfo(userToken).getUid();
        } catch (FeignException ex) {
            throw new IdentityManagerResponseException("Could not get system user id from IDAM", ex);
        }
    }

}
