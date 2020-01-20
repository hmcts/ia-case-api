package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.iacaseapi.Application;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.wiremock.DocumentsApiCallbackTransformer;

@ActiveProfiles("integration")
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
    "S2S_URL=http://127.0.0.1:8990/serviceAuth",
    "IDAM_URL=http://127.0.0.1:8990/userAuth",
    "IA_CASE_DOCUMENTS_API_URL=http://localhost:8990/ia-case-documents-api",
    "PROF_REF_DATA_URL=http://localhost:8990",
    "CCD_URL=http://127.0.0.1:8990/ccd-data-store",
    "IA_DOCMOSIS_ENABLED=true"})
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class SpringBootIntegrationTest {

    @Value("classpath:idam-jwks.json")
    private Resource resourceJwksFile;

    private DocumentsApiCallbackTransformer documentsApiCallbackTransformer = new DocumentsApiCallbackTransformer();

    protected GivensBuilder given;
    protected IaCaseApiClient iaCaseApiClient;
    protected DocumentCaseApiVerifications then;

    @LocalServerPort
    protected int port;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
        wireMockConfig()
            .port(8990)
            .extensions(documentsApiCallbackTransformer));

    @Before
    public void setUpGivens() {
        given = new GivensBuilder(documentsApiCallbackTransformer, resourceJwksFile);
    }

    @Before
    public void setUpVerifications() {
        then = new DocumentCaseApiVerifications();
    }

    @Before
    public void setUpApiClient() {
        iaCaseApiClient = new IaCaseApiClient(port);
    }

    @Before
    public void setupServiceAuthStub() {

        stubFor(get(urlEqualTo("/serviceAuth/details"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("ia")));

        stubFor(post(urlEqualTo("/serviceAuth/lease"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyfQ.L8i6g3PfcHlioHCCPURC9pmXT7gdJpx3kOoyAfNUwCc")));
    }
}
