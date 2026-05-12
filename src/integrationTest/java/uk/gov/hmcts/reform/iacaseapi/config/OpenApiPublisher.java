package uk.gov.hmcts.reform.iacaseapi.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.microsoft.applicationinsights.web.internal.WebRequestTrackingFilter;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

/**
 * Built-in feature which saves service's swagger specs in temporary directory.
 * Each travis run on master should automatically save and upload (if updated) documentation.
 */
@AutoConfigureMockMvc
@SpringJUnitWebConfig
@SpringBootTest
@ActiveProfiles("integration")
@DirtiesContext
class OpenApiPublisher {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext wac;

    @BeforeEach
    void setUp() {
        WebRequestTrackingFilter filter;
        filter = new WebRequestTrackingFilter();
        filter.init(new MockFilterConfig());
        mvc = webAppContextSetup(wac).addFilters(filter).build();
    }

    @DisplayName("Generate swagger documentation")
    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void generateDocs() throws Exception {
        byte[] specs = mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

        try (OutputStream outputStream = Files.newOutputStream(Paths.get("/tmp/swagger-specs.json"))) {
            outputStream.write(specs);
        }

    }
}
