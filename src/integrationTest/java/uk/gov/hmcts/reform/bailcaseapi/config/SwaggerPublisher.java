package uk.gov.hmcts.reform.bailcaseapi.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.microsoft.applicationinsights.web.internal.WebRequestTrackingFilter;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@AutoConfigureMockMvc
@SpringJUnitConfig
@SpringBootTest
@ActiveProfiles("integration")
public class SwaggerPublisher {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext wac;

    @BeforeEach
    public void setUp() {
        WebRequestTrackingFilter filter;
        filter = new WebRequestTrackingFilter();
        filter.init(new MockFilterConfig());
        mvc = webAppContextSetup(wac).addFilter(filter).build();
    }

    @Test
    public void generateDocs() throws Exception {
        byte[] specs = mvc.perform(get("/v2/api-docs"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

        try (OutputStream outputStream = Files.newOutputStream(Paths.get(
            System.getProperty("java.io.tmpdir"),
            "/swagger-specs.json"
        ))) {
            outputStream.write(specs);
        }
    }
}
