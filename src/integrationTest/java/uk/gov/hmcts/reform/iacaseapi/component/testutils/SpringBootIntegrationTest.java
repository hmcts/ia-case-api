package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.web.internal.WebRequestTrackingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.iacaseapi.Application;


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
    "PROF_REF_DATA_URL=http://localhost:8990",
    "CCD_URL=http://127.0.0.1:8990/ccd-data-store",
    "AAC_URL=http://127.0.0.1:8990",
    "IA_TIMED_EVENT_SERVICE_URL=http://127.0.0.1:8990/timed-event-service",
    "IA_DOCMOSIS_ENABLED=true"})
/*@ExtendWith({
    WiremockResolver.class
})*/
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

}
