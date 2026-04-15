package uk.gov.hmcts.reform.iacaseapi.util;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdentityManagerResponseException;

@Service
@Slf4j
public class IdamAuthProvider {

    @Value("${idam.redirectUrl}")
    protected String idamRedirectUri;

    @Value("${idam.scope}") 
    protected String userScope;

    @Value("${spring.security.oauth2.client.registration.oidc.client-id}")
    protected String idamClientId;

    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}")
    protected String idamClientSecret;

    @Autowired
    private IdamApi idamApi;

    @Autowired
    private IdamService idamService;

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

    @Cacheable(value = "legalRepATokenCache", key = "'legalRepATokenCache'")
    public String getLegalRepToken() {
        return getUserToken(
            System.getenv("TEST_LAW_FIRM_A_USERNAME"),
            System.getenv("TEST_LAW_FIRM_A_PASSWORD")
        );
    }

    @Cacheable(value = "caseOfficerTokenCache", key = "'caseOfficerTokenCache'")
    public String getCaseOfficerToken() {
        return getUserToken(
            System.getenv("TEST_CASEOFFICER_USERNAME"),
            System.getenv("TEST_CASEOFFICER_PASSWORD")
        );
    }

    @Cacheable(value = "adminOfficerTokenCache", key = "'adminOfficerTokenCache'")
    public String getAdminOfficerToken() {
        return getUserToken(
            System.getenv("TEST_ADMINOFFICER_USERNAME"),
            System.getenv("TEST_ADMINOFFICER_PASSWORD")
        );
    }

    @Cacheable(value = "homeOfficeApcTokenCache", key = "'homeOfficeApcTokenCache'")
    public String getHomeOfficeApcToken() {
        return getUserToken(
            System.getenv("TEST_HOMEOFFICE_APC_USERNAME"),
            System.getenv("TEST_HOMEOFFICE_APC_PASSWORD")
        );
    }

    @Cacheable(value = "homeOfficeLartTokenCache", key = "'homeOfficeLartTokenCache'")
    public String getHomeOfficeLartToken() {
        return getUserToken(
            System.getenv("TEST_HOMEOFFICE_LART_USERNAME"),
            System.getenv("TEST_HOMEOFFICE_LART_PASSWORD")
        );
    }

    @Cacheable(value = "homeOfficePouTokenCache", key = "'homeOfficePouTokenCache'")
    public String getHomeOfficePouToken() {
        return getUserToken(
            System.getenv("TEST_HOMEOFFICE_POU_USERNAME"),
            System.getenv("TEST_HOMEOFFICE_POU_PASSWORD")
        );
    }

    @Cacheable(value = "homeOfficeGenericTokenCache", key = "'homeOfficeGenericTokenCache'")
    public String getHomeOfficeGenericToken() {
        return getUserToken(
            System.getenv("TEST_HOMEOFFICE_GENERIC_USERNAME"),
            System.getenv("TEST_HOMEOFFICE_GENERIC_PASSWORD")
        );
    }

    @Cacheable(value = "legalRepShareCaseATokenCache", key = "'legalRepShareCaseATokenCache'")
    public String getLegalRepShareCaseAToken() {
        return getUserToken(
            System.getenv("TEST_LAW_FIRM_SHARE_CASE_A_USERNAME"),
            System.getenv("TEST_LAW_FIRM_SHARE_CASE_A_PASSWORD")
        );
    }

    @Cacheable(value = "legalRepOrgSuccessTokenCache", key = "'legalRepOrgSuccessTokenCache'")
    public String getLegalRepOrgSuccessToken() {
        return getUserToken(
            System.getenv("TEST_LAW_FIRM_ORG_SUCCESS_USERNAME"),
            System.getenv("TEST_LAW_FIRM_ORG_SUCCESS_PASSWORD")
        );
    }

    @Cacheable(value = "legalRepOrgDeletedTokenCache", key = "'legalRepOrgDeletedTokenCache'")
    public String getLegalRepOrgDeletedToken() {
        return getUserToken(
            System.getenv("TEST_LAW_FIRM_ORG_DELETED_USERNAME"),
            System.getenv("TEST_LAW_FIRM_ORG_DELETED_PASSWORD")
        );
    }

    @Cacheable(value = "judgeTokenCache", key = "'judgeTokenCache'")
    public String getJudgeToken() {
        return getUserToken(
            System.getenv("TEST_JUDGE_X_USERNAME"),
            System.getenv("TEST_JUDGE_X_PASSWORD")
        );
    }

    @Cacheable(value = "citizenTokenCache", key = "'citizenTokenCache'")
    public String getCitizenToken() {
        return getUserToken(
            System.getenv("TEST_CITIZEN_USERNAME"),
            System.getenv("TEST_CITIZEN_PASSWORD")
        );
    }

    @Cacheable(value = "systemTokenCache", key = "'systemTokenCache'")
    public String getSystemToken() {
        return getUserToken(
            System.getenv("IA_SYSTEM_USERNAME"),
            System.getenv("IA_SYSTEM_PASSWORD")
        );
    }

    public String getUserId(String token) {
        try {
            UserInfo userInfo = idamService.getUserInfo(token);
            return userInfo.getUid();
        } catch (FeignException ex) {
            throw new IdentityManagerResponseException("Could not get system user token from IDAM", ex);
        }
    }
}
