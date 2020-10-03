package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.iacaseapi.Application;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.wiremock.DocumentsApiCallbackTransformer;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdamUserDetailsProvider;

@ActiveProfiles("integration")
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
    "S2S_URL=http://127.0.0.1:8990/serviceAuth",
    "IDAM_URL=http://127.0.0.1:8990/userAuth",
    "OPEN_ID_IDAM_URL=http://127.0.0.1:8990/userAuth",
    "IA_CASE_DOCUMENTS_API_URL=http://localhost:8990/ia-case-documents-api",
    "PROF_REF_DATA_URL=http://localhost:8990",
    "CCD_URL=http://127.0.0.1:8990/ccd-data-store",
    "IA_TIMED_EVENT_SERVICE_URL=http://127.0.0.1:8990/timed-event-service",
    "IA_DOCMOSIS_ENABLED=true"})
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(classes = {TestConfiguration.class, Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class SpringBootIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected CacheManager cacheManager;

    private DocumentsApiCallbackTransformer documentsApiCallbackTransformer = new DocumentsApiCallbackTransformer();

    protected GivensBuilder given;
    protected IaCaseApiClient iaCaseApiClient;
    protected DocumentCaseApiVerifications then;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
        wireMockConfig()
            .port(8990)
            .extensions(documentsApiCallbackTransformer));

    @Autowired
    protected ObjectMapper objectMapper;

    @Before
    public void setUpCacheManager() {
        this.cacheManager.getCache("IdamUserDetails").clear();
    }

    @Before
    public void setUpGivens() {
        given = new GivensBuilder(documentsApiCallbackTransformer);
    }

    @Before
    public void setUpVerifications() {
        then = new DocumentCaseApiVerifications();
    }

    @Before
    public void setUpApiClient() {
        iaCaseApiClient = new IaCaseApiClient(objectMapper, mockMvc);
    }

    @Before
    public void setupServiceAuthStub() {

        stubFor(post(urlEqualTo("/serviceAuth/lease"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyfQ.L8i6g3PfcHlioHCCPURC9pmXT7gdJpx3kOoyAfNUwCc")));
    }
}
