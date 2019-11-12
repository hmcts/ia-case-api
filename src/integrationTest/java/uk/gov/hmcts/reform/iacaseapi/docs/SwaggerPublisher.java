package uk.gov.hmcts.reform.iacaseapi.docs;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.File;
import java.io.FileOutputStream;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.iacaseapi.Application;

/**
 * Built-in feature which saves service's swagger specs in temporary directory.
 * Each travis run on master should automatically save and upload (if updated) documentation.
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = MOCK)
@ActiveProfiles("integration")
public class SwaggerPublisher {

    private static final String SWAGGER_DOC_JSON_FILE = "/tmp/swagger-specs.json";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void shouldGenerateDocs() throws Exception {

        log.info("Generating Swagger Docs");

        File linuxTmpDir = new File("/tmp");
        if (!linuxTmpDir.exists()) {
            return;
        }

        byte[] specs = mockMvc.perform(get("/v2/api-docs"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

        try (FileOutputStream outputStream = new FileOutputStream(SWAGGER_DOC_JSON_FILE)) {
            outputStream.write(specs);
        }

        log.info("Completed Generating Swagger docs to the following location {}",
            SWAGGER_DOC_JSON_FILE);
    }
}
