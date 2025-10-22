package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;

@Slf4j
@Component
public class IdamService {

    private final String systemUserName;
    private final String systemUserPass;
    private final String idamRedirectUrl;
    private final String systemUserScope;
    private final String idamClientId;
    private final String idamClientSecret;
    private final IdamApi idamApi;
    private final RoleAssignmentService roleAssignmentService;
    public static final List<String> amOnboardedRoles =
        List.of("caseworker-ia-caseofficer", "caseworker-ia-iacjudge", "caseworker-ia-admofficer");

    public IdamService(
        @Value("${idam.ia_system_user.username}") String systemUserName,
        @Value("${idam.ia_system_user.password}") String systemUserPass,
        @Value("${idam.redirectUrl}") String idamRedirectUrl,
        @Value("${idam.ia_system_user.scope}") String scope,
        @Value("${spring.security.oauth2.client.registration.oidc.client-id}") String idamClientId,
        @Value("${spring.security.oauth2.client.registration.oidc.client-secret}") String idamClientSecret,
        IdamApi idamApi,
        RoleAssignmentService roleAssignmentService
    ) {
        this.systemUserName = systemUserName;
        this.systemUserPass = systemUserPass;
        this.idamRedirectUrl = idamRedirectUrl;
        this.systemUserScope = scope;
        this.idamClientId = idamClientId;
        this.idamClientSecret = idamClientSecret;
        this.idamApi = idamApi;
        this.roleAssignmentService = roleAssignmentService;
    }

    @Cacheable(value = "accessTokenCache")
    public String getServiceUserToken() {
        log.info("Getting system user token from IDAM");
        Map<String, String> idamAuthDetails = new ConcurrentHashMap<>();

        idamAuthDetails.put("grant_type", "password");
        idamAuthDetails.put("redirect_uri", idamRedirectUrl);
        idamAuthDetails.put("client_id", idamClientId);
        idamAuthDetails.put("client_secret", idamClientSecret);
        idamAuthDetails.put("username", systemUserName);
        idamAuthDetails.put("password", systemUserPass);
        idamAuthDetails.put("scope", systemUserScope);

        for (Map.Entry<String, String> entry : idamAuthDetails.entrySet()) {
            log.info("idamAuthDetails - {}: {}", entry.getKey(), entry.getValue());
        }

        logIdamEnvironmentVariables();

        return "Bearer " + idamApi.token(idamAuthDetails).getAccessToken();
    }

    private void logIdamEnvironmentVariables() {
        log.info("OPEN_ID_IDAM_URL: {}", System.getenv("OPEN_ID_IDAM_URL"));
        log.info("IA_IDAM_REDIRECT_URI: {}", System.getenv("IA_IDAM_REDIRECT_URI"));
        log.info("IA_SYSTEM_USERNAME: {}", System.getenv("IA_SYSTEM_USERNAME"));
        log.info("IA_SYSTEM_PASSWORD: {}", System.getenv("IA_SYSTEM_PASSWORD"));
        log.info("IA_S2S_SECRET: {}", System.getenv("IA_S2S_SECRET"));
        log.info("IA_S2S_MICROSERVICE: {}", System.getenv("IA_S2S_MICROSERVICE"));
        log.info("S2S_URL: {}", System.getenv("S2S_URL"));
        log.info("IA_S2S_AUTHORIZED_SERVICES: {}", System.getenv("IA_S2S_AUTHORIZED_SERVICES"));
        log.info("IA_IDAM_CLIENT_ID: {}", System.getenv("IA_IDAM_CLIENT_ID"));
        log.info("IA_IDAM_SECRET: {}", System.getenv("IA_IDAM_SECRET"));
    }

    @Cacheable(value = "userInfoCache")
    public UserInfo getUserInfo(String accessToken) {
        UserInfo userInfo = idamApi.userInfo(accessToken);
        List<String> amRoles = Collections.emptyList();
        List<String> idamRoles = userInfo.getRoles() == null ?
            Collections.emptyList() :
            userInfo.getRoles();
        try {
            amRoles = roleAssignmentService.getAmRolesFromUser(userInfo.getUid(), accessToken);
        } catch (Exception e) {
            if (idamRoles.stream().anyMatch(amOnboardedRoles::contains)) {
                log.error("Error fetching AM roles for user: {}", userInfo.getUid(), e);
            }
        }
        List<String> roles = Stream.concat(amRoles.stream(), idamRoles.stream()).toList();
        userInfo.setRoles(roles);
        return userInfo;
    }
}
