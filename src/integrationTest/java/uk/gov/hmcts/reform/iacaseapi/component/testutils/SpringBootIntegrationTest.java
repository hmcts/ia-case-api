package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.microsoft.applicationinsights.web.internal.WebRequestTrackingFilter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.iacaseapi.Application;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.wiremock.DocumentsApiCallbackTransformer;
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
    "AAC_URL=http://127.0.0.1:8990",
    "ROLE_ASSIGNMENT_URL=http://127.0.0.1:8990/amRoleAssignment",
    "IA_TIMED_EVENT_SERVICE_URL=http://127.0.0.1:8990/timed-event-service",
    "IA_DOCMOSIS_ENABLED=true",
    "IA_IDAM_CLIENT_ID=ia",
    "IA_IDAM_SECRET=something",
    "IA_S2S_SECRET=AAAAAAAAAAAAAAAC",
    "IA_S2S_MICROSERVICE=ia"})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
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
            .extensions(new DocumentsApiCallbackTransformer(), new NotificationsApiCallbackTransformer())
            .port(8990));
        server.start();
    }

    @BeforeEach
    void setUp() {
        WebRequestTrackingFilter filter;
        filter = new WebRequestTrackingFilter();
        filter.init(new MockFilterConfig());
        mockMvc = webAppContextSetup(wac).addFilters(filter).build();
    }

    protected IaCaseApiClient iaCaseApiClient;

    @BeforeEach
    public void setUpApiClient() {
        iaCaseApiClient = new IaCaseApiClient(objectMapper, mockMvc);
    }

    @AfterEach
    public void reset() {
        server.resetMappings();
        server.resetRequests();
        server.resetScenarios();
        server.resetAll();
    }

    @AfterAll
    @SneakyThrows
    @SuppressWarnings("java:S2925")
    public void shutDown() {
        server.stop();
        /*
            We are not using Wiremock the way it's intended to be used. It should be used by
            starting a webserver at the beginning of all tests and taking it down at the end, but
            what we do is spinning up and down the server all the time and change its mappings
            all the time.
            The result is that its behaviour is somewhat flaky.

            The following pause is meant to allow Wiremock time to conclude some operations that
            we invoke.
         */
        Thread.sleep(1000);
    }

}
