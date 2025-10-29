package uk.gov.hmcts.reform.iacaseapi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.RequestUserAccessTokenProvider;
import uk.gov.hmcts.reform.iacaseapi.util.AuthorizationHeadersProvider;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("functional")
@DirtiesContext
class ConfigValidationTest {

    @MockBean
    RequestUserAccessTokenProvider requestUserAccessTokenProvider;

    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;

    @BeforeEach
    void authenticateMe() {
        String accessToken = authorizationHeadersProvider.getCaseOfficerAuthorization().getValue("Authorization");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(accessToken);
        when(requestUserAccessTokenProvider.getAccessToken()).thenReturn(accessToken);
    }

    @Autowired
    FeatureToggler featureToggler;

    @ParameterizedTest
    @CsvSource("use-ccd-document-am")
    void launchDarklyFeatureTogglesPresent(String featureToggleName) {
        boolean value1 = featureToggler.getValue(featureToggleName, true);
        boolean value2 = featureToggler.getValue(featureToggleName, false);

        // As a feature toggle cannot be both true and false at the same time, if the values returned don't match
        // it means they are both using their default... which in turn means that the feature toggle is not
        // configured or the connection to LaunchDarkly is not working.
        //
        // You can access LaunchDarkly through MyApps (https://myapps.microsoft.com/).
        // Switch to the "Immigration and Asylum" project and use the appropriate environment:
        //  "Test" -> "preview" or "demo"
        //  "Production" -> "aat" or "production"

        Assertions.assertEquals(value1, value2,
            "The feature toggle " + featureToggleName + " may not be present " +
                "or the connection to LaunchDarkly may not be working. " +
                "Check the code of the failing test for further information");
    }
}
