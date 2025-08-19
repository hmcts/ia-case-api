package uk.gov.hmcts.reform.iacaseapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.iacaseapi.fixtures.DocumentManagementFilesFixture;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.ServiceTokenGeneratorConfiguration;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ExtendedCcdApi;
import uk.gov.hmcts.reform.iacaseapi.util.FunctionalSpringContext;
import uk.gov.hmcts.reform.iacaseapi.util.IdamAuthProvider;
import uk.gov.hmcts.reform.iacaseapi.util.MapValueExpander;


@SpringBootTest(classes = {
    ServiceTokenGeneratorConfiguration.class,
    FunctionalSpringContext.class
})
@ActiveProfiles("functional")
public class FunctionalTest {
    @Value("${idam.redirectUrl}")
    protected String idamRedirectUri;
    @Value("${idam.scope}")
    protected String userScope;
    @Value("${spring.security.oauth2.client.registration.oidc.client-id}")
    protected String idamClientId;
    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}")
    protected String idamClientSecret;
    @Autowired
    protected IdamApi idamApi;

    @Value("classpath:templates/minimal-appeal-started.json")
    protected Resource minimalAppealStarted;

    @Autowired
    protected AuthTokenGenerator s2sAuthTokenGenerator;

    @Autowired
    protected IdamAuthProvider idamAuthProvider;

    @Autowired
    protected ExtendedCcdApi ccdApi;

    @Autowired
    protected CaseDocumentClient caseDocumentClient;

    @Autowired
    protected DocumentManagementFilesFixture documentManagementFilesFixture;

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:8090"
        );

    protected RequestSpecification requestSpecification;

    @Autowired
    protected MapValueExpander mapValueExpander;

    @BeforeEach
    public void setup() throws IOException {
        requestSpecification = new RequestSpecBuilder()
            .setBaseUri(targetInstance)
            .setRelaxedHTTPSValidation()
            .build();

        documentManagementFilesFixture.prepare();
    }
}
