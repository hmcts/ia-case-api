package uk.gov.hmcts.reform.iacaseapi.util;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;

@Service
public class AuthorizationHeadersProvider {

    @Value("${idam.redirectUrl}") protected String idamRedirectUrl;
    @Value("${idam.scope}") protected String userScope;
    @Value("${spring.security.oauth2.client.registration.oidc.client-id}") protected String idamClientId;
    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}") protected String idamClientSecret;

    @Autowired
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @Autowired
    private IdamApi idamApi;

    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    public Headers getLegalRepresentativeAuthorization() {

        MultiValueMap<String, String> tokenRequestForm = new LinkedMultiValueMap<>();
        tokenRequestForm.add("grant_type", "password");
        tokenRequestForm.add("redirect_uri", idamRedirectUrl);
        tokenRequestForm.add("client_id", idamClientId);
        tokenRequestForm.add("client_secret", idamClientSecret);
        tokenRequestForm.add("username", System.getenv("TEST_LAW_FIRM_A_USERNAME"));
        tokenRequestForm.add("password", System.getenv("TEST_LAW_FIRM_A_PASSWORD"));
        tokenRequestForm.add("scope", userScope);

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "LegalRepresentative",
            user -> "Bearer " + idamApi.token(tokenRequestForm).getAccessToken()
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getSystemUserAuthorization() {

        MultiValueMap<String, String> tokenRequestForm = new LinkedMultiValueMap<>();
        tokenRequestForm.add("grant_type", "password");
        tokenRequestForm.add("redirect_uri", idamRedirectUrl);
        tokenRequestForm.add("client_id", idamClientId);
        tokenRequestForm.add("client_secret", idamClientSecret);
        tokenRequestForm.add("username", System.getenv("IA_SYSTEM_USERNAME"));
        tokenRequestForm.add("password", System.getenv("IA_SYSTEM_PASSWORD"));
        tokenRequestForm.add("scope", userScope);

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "SystemUser",
            user -> "Bearer " + idamApi.token(tokenRequestForm).getAccessToken()
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getCaseOfficerAuthorization() {

        MultiValueMap<String, String> tokenRequestForm = new LinkedMultiValueMap<>();
        tokenRequestForm.add("grant_type", "password");
        tokenRequestForm.add("redirect_uri", idamRedirectUrl);
        tokenRequestForm.add("client_id", idamClientId);
        tokenRequestForm.add("client_secret", idamClientSecret);
        tokenRequestForm.add("username", System.getenv("TEST_CASEOFFICER_USERNAME"));
        tokenRequestForm.add("password", System.getenv("TEST_CASEOFFICER_PASSWORD"));
        tokenRequestForm.add("scope", userScope);

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "CaseOfficer",
            user -> "Bearer " + idamApi.token(tokenRequestForm).getAccessToken()
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getAdminOfficerAuthorization() {

        MultiValueMap<String, String> tokenRequestForm = new LinkedMultiValueMap<>();
        tokenRequestForm.add("grant_type", "password");
        tokenRequestForm.add("redirect_uri", idamRedirectUrl);
        tokenRequestForm.add("client_id", idamClientId);
        tokenRequestForm.add("client_secret", idamClientSecret);
        tokenRequestForm.add("username", System.getenv("TEST_ADMINOFFICER_USERNAME"));
        tokenRequestForm.add("password", System.getenv("TEST_ADMINOFFICER_PASSWORD"));
        tokenRequestForm.add("scope", userScope);

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "AdminOfficer",
            user -> "Bearer " + idamApi.token(tokenRequestForm).getAccessToken()
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getHomeOfficeApcAuthorization() {

        MultiValueMap<String, String> tokenRequestForm = new LinkedMultiValueMap<>();
        tokenRequestForm.add("grant_type", "password");
        tokenRequestForm.add("redirect_uri", idamRedirectUrl);
        tokenRequestForm.add("client_id", idamClientId);
        tokenRequestForm.add("client_secret", idamClientSecret);
        tokenRequestForm.add("username", System.getenv("TEST_HOMEOFFICE_APC_USERNAME"));
        tokenRequestForm.add("password", System.getenv("TEST_HOMEOFFICE_APC_PASSWORD"));
        tokenRequestForm.add("scope", userScope);

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "HomeOfficeApc",
            user -> "Bearer " + idamApi.token(tokenRequestForm).getAccessToken()
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getHomeOfficeLartAuthorization() {

        MultiValueMap<String, String> tokenRequestForm = new LinkedMultiValueMap<>();
        tokenRequestForm.add("grant_type", "password");
        tokenRequestForm.add("redirect_uri", idamRedirectUrl);
        tokenRequestForm.add("client_id", idamClientId);
        tokenRequestForm.add("client_secret", idamClientSecret);
        tokenRequestForm.add("username", System.getenv("TEST_HOMEOFFICE_LART_USERNAME"));
        tokenRequestForm.add("password", System.getenv("TEST_HOMEOFFICE_LART_PASSWORD"));
        tokenRequestForm.add("scope", userScope);

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "HomeOfficeLart",
            user -> "Bearer " + idamApi.token(tokenRequestForm).getAccessToken()
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getHomeOfficePouAuthorization() {

        MultiValueMap<String, String> tokenRequestForm = new LinkedMultiValueMap<>();
        tokenRequestForm.add("grant_type", "password");
        tokenRequestForm.add("redirect_uri", idamRedirectUrl);
        tokenRequestForm.add("client_id", idamClientId);
        tokenRequestForm.add("client_secret", idamClientSecret);
        tokenRequestForm.add("username", System.getenv("TEST_HOMEOFFICE_POU_USERNAME"));
        tokenRequestForm.add("password", System.getenv("TEST_HOMEOFFICE_POU_PASSWORD"));
        tokenRequestForm.add("scope", userScope);

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "HomeOfficePou",
            user -> "Bearer " + idamApi.token(tokenRequestForm).getAccessToken()
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getHomeOfficeGenericAuthorization() {

        MultiValueMap<String, String> tokenRequestForm = new LinkedMultiValueMap<>();
        tokenRequestForm.add("grant_type", "password");
        tokenRequestForm.add("redirect_uri", idamRedirectUrl);
        tokenRequestForm.add("client_id", idamClientId);
        tokenRequestForm.add("client_secret", idamClientSecret);
        tokenRequestForm.add("username", System.getenv("TEST_HOMEOFFICE_GENERIC_USERNAME"));
        tokenRequestForm.add("password", System.getenv("TEST_HOMEOFFICE_GENERIC_PASSWORD"));
        tokenRequestForm.add("scope", userScope);

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "HomeOfficeGeneric",
            user -> "Bearer " + idamApi.token(tokenRequestForm).getAccessToken()
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getLegalRepresentativeOrgAAuthorization() {

        MultiValueMap<String, String> tokenRequestForm = new LinkedMultiValueMap<>();
        tokenRequestForm.add("grant_type", "password");
        tokenRequestForm.add("redirect_uri", idamRedirectUrl);
        tokenRequestForm.add("client_id", idamClientId);
        tokenRequestForm.add("client_secret", idamClientSecret);
        tokenRequestForm.add("username", System.getenv("TEST_LAW_FIRM_SHARE_CASE_A_USERNAME"));
        tokenRequestForm.add("password", System.getenv("TEST_LAW_FIRM_SHARE_CASE_A_PASSWORD"));
        tokenRequestForm.add("scope", userScope);

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "LegalRepresentativeOrgA",
            user -> "Bearer " + idamApi.token(tokenRequestForm).getAccessToken()
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getLegalRepresentativeOrgSuccessAuthorization() {

        MultiValueMap<String, String> tokenRequestForm = new LinkedMultiValueMap<>();
        tokenRequestForm.add("grant_type", "password");
        tokenRequestForm.add("redirect_uri", idamRedirectUrl);
        tokenRequestForm.add("client_id", idamClientId);
        tokenRequestForm.add("client_secret", idamClientSecret);
        tokenRequestForm.add("username", System.getenv("TEST_LAW_FIRM_ORG_SUCCESS_USERNAME"));
        tokenRequestForm.add("password", System.getenv("TEST_LAW_FIRM_ORG_SUCCESS_PASSWORD"));
        tokenRequestForm.add("scope", userScope);

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "LegalRepresentativeOrgSuccess",
            user -> "Bearer " + idamApi.token(tokenRequestForm).getAccessToken()
        );

        return new Headers(
                new Header("ServiceAuthorization", serviceToken),
                new Header("Authorization", accessToken)
        );
    }

    public Headers getLegalRepresentativeOrgDeletedAuthorization() {

        MultiValueMap<String, String> tokenRequestForm = new LinkedMultiValueMap<>();
        tokenRequestForm.add("grant_type", "password");
        tokenRequestForm.add("redirect_uri", idamRedirectUrl);
        tokenRequestForm.add("client_id", idamClientId);
        tokenRequestForm.add("client_secret", idamClientSecret);
        tokenRequestForm.add("username", System.getenv("TEST_LAW_FIRM_ORG_DELETED_USERNAME"));
        tokenRequestForm.add("password", System.getenv("TEST_LAW_FIRM_ORG_DELETED_PASSWORD"));
        tokenRequestForm.add("scope", userScope);

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "LegalRepresentativeOrgDeleted",
            user -> "Bearer " + idamApi.token(tokenRequestForm).getAccessToken()
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getJudgeAuthorization() {

        MultiValueMap<String, String> tokenRequestForm = new LinkedMultiValueMap<>();
        tokenRequestForm.add("grant_type", "password");
        tokenRequestForm.add("redirect_uri", idamRedirectUrl);
        tokenRequestForm.add("client_id", idamClientId);
        tokenRequestForm.add("client_secret", idamClientSecret);
        tokenRequestForm.add("username", System.getenv("TEST_JUDGE_X_USERNAME"));
        tokenRequestForm.add("password", System.getenv("TEST_JUDGE_X_PASSWORD"));
        tokenRequestForm.add("scope", userScope);

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "Judge",
            user -> "Bearer " + idamApi.token(tokenRequestForm).getAccessToken()
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getCitizenAuthorization() {

        MultiValueMap<String, String> tokenRequestForm = new LinkedMultiValueMap<>();
        tokenRequestForm.add("grant_type", "password");
        tokenRequestForm.add("redirect_uri", idamRedirectUrl);
        tokenRequestForm.add("client_id", idamClientId);
        tokenRequestForm.add("client_secret", idamClientSecret);
        tokenRequestForm.add("username", System.getenv("TEST_CITIZEN_USERNAME"));
        tokenRequestForm.add("password", System.getenv("TEST_CITIZEN_PASSWORD"));
        tokenRequestForm.add("scope", userScope);

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "Citizen",
            user -> "Bearer " + idamApi.token(tokenRequestForm).getAccessToken()
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }
}
