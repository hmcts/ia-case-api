package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamClientApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.User;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdentityManagerResponseException;

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
    private final IdamClientApi idamClientApi;
    private final RoleAssignmentService roleAssignmentService;
    public static final List<String> amOnboardedRoles =
        List.of("caseworker-ia-caseofficer", "caseworker-ia-iacjudge", "caseworker-ia-admofficer");

    private String idamClientToken = "idamToken";

    public IdamService(
        @Value("${idam.ia_system_user.username}") String systemUserName,
        @Value("${idam.ia_system_user.password}") String systemUserPass,
        @Value("${idam.redirectUrl}") String idamRedirectUrl,
        @Value("${idam.ia_system_user.scope}") String scope,
        @Value("${spring.security.oauth2.client.registration.oidc.client-id}") String idamClientId,
        @Value("${spring.security.oauth2.client.registration.oidc.client-secret}") String idamClientSecret,
        IdamApi idamApi,
        IdamClientApi idamClientApi,
        RoleAssignmentService roleAssignmentService
    ) {
        this.systemUserName = systemUserName;
        this.systemUserPass = systemUserPass;
        this.idamRedirectUrl = idamRedirectUrl;
        this.systemUserScope = scope;
        this.idamClientId = idamClientId;
        this.idamClientSecret = idamClientSecret;
        this.idamApi = idamApi;
        this.idamClientApi = idamClientApi;
        this.roleAssignmentService = roleAssignmentService;
    }

    @Cacheable(value = "systemUserTokenCache", key = "'systemUserTokenCache'")
    public String getServiceUserToken() {
        Map<String, String> idamAuthDetails = new ConcurrentHashMap<>();

        idamAuthDetails.put("grant_type", "password");
        idamAuthDetails.put("redirect_uri", idamRedirectUrl);
        idamAuthDetails.put("client_id", idamClientId);
        idamAuthDetails.put("client_secret", idamClientSecret);
        idamAuthDetails.put("username", systemUserName);
        idamAuthDetails.put("password", systemUserPass);
        idamAuthDetails.put("scope", systemUserScope);

        log.info("System user token expired. Getting a new token in ia-case-api");
        return "Bearer " + idamApi.token(idamAuthDetails).getAccessToken();
    }

    @Cacheable(value = "userInfoCache", key = "#accessToken")
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

    @Cacheable(value = "clientCredsCache", key = "'clientCredsCache'")
    public String getClientCredentialsToken() {
        try {
            Map<String, String> idamAuthDetails = new ConcurrentHashMap<>();
            idamAuthDetails.put("grant_type", "client_credentials");
            idamAuthDetails.put("redirect_uri", idamRedirectUrl);
            idamAuthDetails.put("client_id", idamClientId);
            idamAuthDetails.put("client_secret", idamClientSecret);
            idamAuthDetails.put("scope", "search-user");
            idamClientToken = idamApi.token(idamAuthDetails).getAccessToken();
        } catch (final Exception exception) {
            String msg = "Unable to generate IDAM token due to error - %s".formatted(exception.getMessage());
            log.error(msg, exception);
            throw new IdentityManagerResponseException(msg, exception);
        }

        return "Bearer " + idamClientToken;
    }

    public User getUserFromId(String userId) {
        String idamToken = getClientCredentialsToken();
        String query = MessageFormat.format("id:{0}", userId);
        ResponseEntity<List<User>> response = idamClientApi.getUser(idamToken, query);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.error("Error fetching user details for userId: {}. Response status: {}", userId, response.getStatusCode());
            return null;
        }
        if (response.getBody().isEmpty()) {
            log.error("No user found for userId: {}", userId);
            return null;
        }
        return response.getBody().getFirst();
    }
}
