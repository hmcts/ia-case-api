package uk.gov.hmcts.reform.iacaseapi.util;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdamAuthorizor;

@Service
public class AuthorizationHeadersProvider {

    @Autowired
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @Autowired
    private IdamAuthorizor idamAuthorizor;

    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    public Headers getLegalRepresentativeAuthorization() {

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "LegalRepresentative",
            user -> idamAuthorizor.exchangeForAccessToken(
                System.getenv("TEST_LAW_FIRM_A_USERNAME"),
                System.getenv("TEST_LAW_FIRM_A_PASSWORD")
            )
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getCaseOfficerAuthorization() {

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "CaseOfficer",
            user -> idamAuthorizor.exchangeForAccessToken(
                System.getenv("TEST_CASEOFFICER_USERNAME"),
                System.getenv("TEST_CASEOFFICER_PASSWORD")
            )
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getAdminOfficerAuthorization() {

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "AdminOfficer",
            user -> idamAuthorizor.exchangeForAccessToken(
                System.getenv("TEST_ADMINOFFICER_USERNAME"),
                System.getenv("TEST_ADMINOFFICER_PASSWORD")
            )
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getHomeOfficeApcAuthorization() {

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "HomeOfficeApc",
            user -> idamAuthorizor.exchangeForAccessToken(
                System.getenv("TEST_HOMEOFFICE_APC_USERNAME"),
                System.getenv("TEST_HOMEOFFICE_APC_PASSWORD")
            )
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getHomeOfficeLartAuthorization() {

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "HomeOfficeLart",
            user -> idamAuthorizor.exchangeForAccessToken(
                System.getenv("TEST_HOMEOFFICE_LART_USERNAME"),
                System.getenv("TEST_HOMEOFFICE_LART_PASSWORD")
            )
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getHomeOfficePouAuthorization() {

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "HomeOfficePou",
            user -> idamAuthorizor.exchangeForAccessToken(
                System.getenv("TEST_HOMEOFFICE_POU_USERNAME"),
                System.getenv("TEST_HOMEOFFICE_POU_PASSWORD")
            )
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getHomeOfficeGenericAuthorization() {

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "HomeOfficeGeneric",
            user -> idamAuthorizor.exchangeForAccessToken(
                System.getenv("TEST_HOMEOFFICE_GENERIC_USERNAME"),
                System.getenv("TEST_HOMEOFFICE_GENERIC_PASSWORD")
            )
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getLegalRepresentativeOrgAAuthorization() {

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "LegalRepresentativeOrgA",
            user -> idamAuthorizor.exchangeForAccessToken(
                System.getenv("TEST_LAW_FIRM_SHARE_CASE_A_USERNAME"),
                System.getenv("TEST_LAW_FIRM_SHARE_CASE_A_PASSWORD")
            )
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getJudgeAuthorization() {

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "Judge",
            user -> idamAuthorizor.exchangeForAccessToken(
                System.getenv("TEST_JUDGE_X_USERNAME"),
                System.getenv("TEST_JUDGE_X_PASSWORD")
            )
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getCitizenAuthorization() {

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            "Citizen",
            user -> idamAuthorizor.exchangeForAccessToken(
                System.getenv("TEST_CITIZEN_USERNAME"),
                System.getenv("TEST_CITIZEN_PASSWORD")
            )
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }
}