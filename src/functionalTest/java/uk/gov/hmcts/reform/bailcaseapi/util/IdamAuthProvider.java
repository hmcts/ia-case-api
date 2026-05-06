package uk.gov.hmcts.reform.bailcaseapi.util;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam.IdentityManagerResponseException;

@Service
public class IdamAuthProvider {
    @Value("${idam.redirectUrl}") protected String idamRedirectUri;
    @Value("${idam.scope}") protected String userScope;
    @Value("${spring.security.oauth2.client.registration.oidc.client-id}") protected String idamClientId;
    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}") protected String idamClientSecret;

    @Autowired
    private IdamApi idamApi;

    public String getUserToken(String username, String password) {

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("redirect_uri", idamRedirectUri);
        map.add("client_id", idamClientId);
        map.add("client_secret", idamClientSecret);
        map.add("username", username);
        map.add("password", password);
        map.add("scope", userScope);
        try {
            Token tokenResponse = idamApi.token(map);
            return "Bearer " + tokenResponse.getAccessToken();
        } catch (FeignException ex) {
            throw new IdentityManagerResponseException("Could not get user token from IDAM", ex);
        }
    }

    @Cacheable(value = "legalRepATokenBailCache", key = "'legalRepATokenBailCache'")
    public String getLegalRepToken() {
        return getUserToken(
            System.getenv("TEST_LAW_FIRM_A_USERNAME"),
            System.getenv("TEST_LAW_FIRM_A_PASSWORD")
        );
    }

    @Cacheable(value = "caseOfficerTokenBailCache", key = "'caseOfficerTokenBailCache'")
    public String getCaseOfficerToken() {
        return getUserToken(
            System.getenv("TEST_CASEOFFICER_USERNAME"),
            System.getenv("TEST_CASEOFFICER_PASSWORD")
        );
    }

    @Cacheable(value = "adminOfficerTokenBailCache", key = "'adminOfficerTokenBailCache'")
    public String getAdminOfficerToken() {
        return getUserToken(
            System.getenv("TEST_ADMINOFFICER_USERNAME"),
            System.getenv("TEST_ADMINOFFICER_PASSWORD")
        );
    }

    @Cacheable(value = "homeOfficeBailTokenBailCache", key = "'homeOfficeBailTokenBailCache'")
    public String getHomeOfficeBailToken() {
        return getUserToken(
            System.getenv("TEST_HOMEOFFICE_BAIL_USERNAME"),
            System.getenv("TEST_HOMEOFFICE_BAIL_PASSWORD")
        );
    }

    @Cacheable(value = "homeOfficeGenericTokenBailCache", key = "'homeOfficeGenericTokenBailCache'")
    public String getHomeOfficeGenericToken() {
        return getUserToken(
            System.getenv("TEST_HOMEOFFICE_GENERIC_USERNAME"),
            System.getenv("TEST_HOMEOFFICE_GENERIC_PASSWORD")
        );
    }

    @Cacheable(value = "legalRepShareCaseATokenBailCache", key = "'legalRepShareCaseATokenBailCache'")
    public String getLegalRepShareCaseAToken() {
        return getUserToken(
            System.getenv("TEST_LAW_FIRM_SHARE_CASE_A_USERNAME"),
            System.getenv("TEST_LAW_FIRM_SHARE_CASE_A_PASSWORD")
        );
    }

    @Cacheable(value = "legalRepOrgSuccessTokenBailCache", key = "'legalRepOrgSuccessTokenBailCache'")
    public String getLegalRepOrgSuccessToken() {
        return getUserToken(
            System.getenv("TEST_LAW_FIRM_ORG_SUCCESS_USERNAME"),
            System.getenv("TEST_LAW_FIRM_ORG_SUCCESS_PASSWORD")
        );
    }

    @Cacheable(value = "judgeTokenBailCache", key = "'judgeTokenBailCache'")
    public String getJudgeToken() {
        return getUserToken(
            System.getenv("TEST_JUDGE_X_USERNAME"),
            System.getenv("TEST_JUDGE_X_PASSWORD")
        );
    }
}
