package uk.gov.hmcts.reform.iacaseapi.util;

import io.restassured.http.Header;
import io.restassured.http.Headers;
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

    public Headers getLegalRepresentativeAuthorization() {

        String serviceToken = serviceAuthTokenGenerator.generate();
        String accessToken = idamAuthorizor.exchangeForAccessToken(
            System.getenv("TEST_LAW_FIRM_A_USERNAME"),
            System.getenv("TEST_LAW_FIRM_A_PASSWORD")
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getCaseOfficerAuthorization() {

        String serviceToken = serviceAuthTokenGenerator.generate();
        String accessToken = idamAuthorizor.exchangeForAccessToken(
            System.getenv("TEST_CASEOFFICER_USERNAME"),
            System.getenv("TEST_CASEOFFICER_PASSWORD")
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getAdminOfficerAuthorization() {

        String serviceToken = serviceAuthTokenGenerator.generate();
        String accessToken = idamAuthorizor.exchangeForAccessToken(
            System.getenv("TEST_ADMINOFFICER_USERNAME"),
            System.getenv("TEST_ADMINOFFICER_PASSWORD")
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getHomeOfficeApcAuthorization() {

        String serviceToken = serviceAuthTokenGenerator.generate();
        String accessToken = idamAuthorizor.exchangeForAccessToken(
            System.getenv("TEST_HOMEOFFICE_APC_USERNAME"),
            System.getenv("TEST_HOMEOFFICE_APC_PASSWORD")
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getHomeOfficeLartAuthorization() {

        String serviceToken = serviceAuthTokenGenerator.generate();
        String accessToken = idamAuthorizor.exchangeForAccessToken(
            System.getenv("TEST_HOMEOFFICE_LART_USERNAME"),
            System.getenv("TEST_HOMEOFFICE_LART_PASSWORD")
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getHomeOfficePouAuthorization() {

        String serviceToken = serviceAuthTokenGenerator.generate();
        String accessToken = idamAuthorizor.exchangeForAccessToken(
            System.getenv("TEST_HOMEOFFICE_POU_USERNAME"),
            System.getenv("TEST_HOMEOFFICE_POU_PASSWORD")
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getHomeOfficeGenericAuthorization() {

        String serviceToken = serviceAuthTokenGenerator.generate();
        String accessToken = idamAuthorizor.exchangeForAccessToken(
            System.getenv("TEST_HOMEOFFICE_GENERIC_USERNAME"),
            System.getenv("TEST_HOMEOFFICE_GENERIC_PASSWORD")
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }
}
