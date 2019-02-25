package uk.gov.hmcts.reform.iacaseapi;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = MOCK)
@ActiveProfiles("integration")
public class BadRequestsTest {

    private MockMvc mockMvc;

    private static final String ABOUT_TO_SUBMIT_PATH = "/asylum/ccdAboutToSubmit";
    private static final String SUBMITTED_PATH = "/asylum/ccdSubmitted";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void shouldRequestUnsupportedMediaTypeToServerAndReceiveHttp415() throws Exception {

        runClientRequest(
            ABOUT_TO_SUBMIT_PATH,
            MediaType.APPLICATION_XML,
            "<xml></xml>",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()
        );

        runClientRequest(
            SUBMITTED_PATH,
            MediaType.APPLICATION_XML,
            "<xml></xml>",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()
        );

        runClientRequest(
            SUBMITTED_PATH,
            MediaType.TEXT_PLAIN,
            "random text",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()
        );
    }

    @Test
    public void shouldMakeBadRequestsToServerAndReceiveHttp4xx() throws Exception {

        runClientRequest(
            ABOUT_TO_SUBMIT_PATH,
            MediaType.APPLICATION_JSON,
            "random string",
            HttpStatus.BAD_REQUEST.value()
        );

        runClientRequest(
            ABOUT_TO_SUBMIT_PATH,
            MediaType.APPLICATION_JSON,
            "{  \"foo\": \"bar\"}",
            HttpStatus.BAD_REQUEST.value()
        );
    }

    private void runClientRequest(
        final String path,
        final MediaType mediaType,
        final String content,
        final int expectedHttpStatus
    ) throws Exception {
        mockMvc.perform(post(path)
            .contentType(mediaType).content(content))
            .andExpect(status().is(expectedHttpStatus)).andReturn();
    }
}
