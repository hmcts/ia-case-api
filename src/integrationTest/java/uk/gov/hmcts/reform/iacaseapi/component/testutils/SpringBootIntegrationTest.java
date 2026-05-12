package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.iacaseapi.Application;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.wiremock.DocumentsApiCallbackTransformer;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.wiremock.HomeOfficeIntegrationApiCallbackTransformer;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.wiremock.NotificationsApiCallbackTransformer;


@SpringBootTest(classes = {
    TestConfiguration.class,
    Application.class
})
@TestPropertySource(properties = {
    "S2S_URL=http://127.0.0.1:8990/serviceAuth",
    "IDAM_URL=http://127.0.0.1:8990/userAuth",
    "OPEN_ID_IDAM_URL=http://127.0.0.1:8990/userAuth",
    "IA_CASE_DOCUMENTS_API_URL=http://localhost:8990/ia-case-documents-api",
    "IA_CASE_NOTIFICATIONS_API_URL=http://localhost:8990/ia-case-notifications-api",
    "prof.ref.data.url=http://localhost:8990",
    "CCD_URL=http://127.0.0.1:8990/ccd-data-store",
    "CCD_CASE_ACCESS_URL=http://127.0.0.1:8990/ccd-data-store",
    "core_case_data.api.url=http://127.0.0.1:8990/ccd-data-store",
    "AAC_URL=http://127.0.0.1:8990",
    "ROLE_ASSIGNMENT_URL=http://127.0.0.1:8990/amRoleAssignment",
    "IA_TIMED_EVENT_SERVICE_URL=http://127.0.0.1:8990/timed-event-service",
    "IA_DOCMOSIS_ENABLED=true",
    "IA_IDAM_CLIENT_ID=ia",
    "spring.retry.maxAttempts=100",
    "IA_HOME_OFFICE_INTEGRATION_API_URL=http://127.0.0.1:8990/home-office-integration-api",
    "IA_S2S_MICROSERVICE=ia"})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class SpringBootIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext wac;

    protected static WireMockServer server;

    @BeforeAll
    public void spinUp() {
        server = new WireMockServer(WireMockConfiguration.options()
            .notifier(new Slf4jNotifier(true))
            .extensions(
                new DocumentsApiCallbackTransformer(),
                new NotificationsApiCallbackTransformer(),
                new HomeOfficeIntegrationApiCallbackTransformer()
            ).port(8990));
        server.start();
    }

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(wac).build();
    }

    protected IaCaseApiClient iaCaseApiClient;

    @BeforeEach
    public void setUpApiClient() {
        iaCaseApiClient = new IaCaseApiClient(objectMapper, mockMvc);
    }

    @AfterEach
    public void reset() {
        server.resetAll();
    }

    @AfterAll
    public void shutDown() {
        server.stop();
    }

}
