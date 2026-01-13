package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PostSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;

@Slf4j
public class IaCaseApiClient {

    public static final String SERVICE_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3O"
                                                    + "DkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJS"
                                                    + "MeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    public static final String USER_JWT_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV"
                                                 + "2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJjY2QtaW1wb3J"
                                                 + "0QGZha2UuaG1jdHMubmV0IiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkI"
                                                 + "joiZDg3ODI3ODQtMWU0NC00NjkyLTg0NzgtNTI5MzE0NTVhNGI5IiwiaXNzIjoiaHR"
                                                 + "0cDovL2ZyLWFtOjgwODAvb3BlbmFtL29hdXRoMi9obWN0cyIsInRva2VuTmFtZSI6I"
                                                 + "mFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI"
                                                 + "6IjNjMWMzNjFkLTRlYzUtNGY0NS1iYzI0LTUxOGMzMDk0MzUxYiIsImF1ZCI6ImNjZ"
                                                 + "F9nYXRld2F5IiwibmJmIjoxNTg0NTI2MzcyLCJncmFudF90eXBlIjoicGFzc3dvcmQ"
                                                 + "iLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiXSwiYXV0aF90aW1lI"
                                                 + "joxNTg0NTI2MzcyLCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTU4NDU1NTE3MiwiaWF"
                                                 + "0IjoxNTg0NTI2MzcyLCJleHBpcmVzX2luIjoyODgwMCwianRpIjoiNDhjNDMzYTQtZ"
                                                 + "mRiNS00YTIwLWFmNGUtMmYyNjIyYjYzZmU5In0.WP8ATcHMmdtG2W443aqNz3ES6-B"
                                                 + "qng0IKjTQfbndN1HrBLJWJtpC3qfzy2wD_CdiPU4uspdN5S91nhiT8Ub6DjstnDz3V"
                                                 + "PmR3Cbdk5QJBdAsQ0ah9w-duS8SA_dlzDIMt18bSDMUUdck6YxsoNyQFisI6cKNnfg"
                                                 + "B9ZTLhenVENtdmyrKVr96Ezp-jhhzmMVMxb1rW7KghSAH0ZCsWqlhrM--jPGRCweDi"
                                                 + "Fe-ldi4EuhIxGbkPjyWwsdcgmYfIuFrSxqV0vrSI37DNZx_Sh5DVJpUgSrYKRzuMqe"
                                                 + "4rFN6WVyHIY_Qu52ER2yrNYtGbAQ5AyMabPTPj9VVxqpa5nYUAg";

    public static final String USER_ID = "49154ae9-47be-4469-9edd-d43f68d245f0";

    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    private final String aboutToSubmitUrl;
    private final String aboutToStartUrl;
    private final String ccdSubmittedUrl;

    public IaCaseApiClient(ObjectMapper objectMapper, MockMvc mockMvc) {
        this.objectMapper = objectMapper;
        this.mockMvc = mockMvc;
        this.aboutToSubmitUrl = "/asylum/ccdAboutToSubmit";
        this.aboutToStartUrl = "/asylum/ccdAboutToStart";
        this.ccdSubmittedUrl = "/asylum/ccdSubmitted";
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
    }

    public PreSubmitCallbackResponseForTest aboutToSubmit(CallbackForTest.CallbackForTestBuilder callback) {

        try {
            MvcResult response = mockMvc
                .perform(
                    post(aboutToSubmitUrl)
                        .header("Authorization", USER_JWT_TOKEN)
                        .header("ServiceAuthorization", SERVICE_JWT_TOKEN)
                        .content(objectMapper.writeValueAsString(callback.build()))
                        .contentType(APPLICATION_JSON_VALUE)
                )
                .andReturn();

            return objectMapper.readValue(
                response.getResponse().getContentAsString(),
                PreSubmitCallbackResponseForTest.class
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // test will fail
            throw new RuntimeException(e);
        }
    }

    public PreSubmitCallbackResponseForTest aboutToStart(CallbackForTest.CallbackForTestBuilder callback) {

        try {
            MvcResult response = mockMvc
                .perform(
                    post(aboutToStartUrl)
                        .header("Authorization", USER_JWT_TOKEN)
                        .header("ServiceAuthorization", SERVICE_JWT_TOKEN)
                        .content(objectMapper.writeValueAsString(callback.build()))
                        .contentType(APPLICATION_JSON_VALUE)
                )
                .andReturn();

            return objectMapper.readValue(
                response.getResponse().getContentAsString(),
                PreSubmitCallbackResponseForTest.class
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // test will fail
            throw new RuntimeException(e);
        }
    }

    public PostSubmitCallbackResponseForTest ccdSubmitted(CallbackForTest.CallbackForTestBuilder callback) {

        try {
            MvcResult response = mockMvc
                .perform(
                    post(ccdSubmittedUrl)
                        .header("Authorization", USER_JWT_TOKEN)
                        .header("ServiceAuthorization", SERVICE_JWT_TOKEN)
                        .content(objectMapper.writeValueAsString(callback.build()))
                        .contentType(APPLICATION_JSON_VALUE)
                )
                .andReturn();

            return objectMapper.readValue(
                response.getResponse().getContentAsString(),
                PostSubmitCallbackResponseForTest.class
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // test will fail
            throw new RuntimeException(e);
        }
    }

    public String supplementaryResponseRequest(String request, ResultMatcher resultMatcher) {
        try {
            MvcResult response = mockMvc
                .perform(
                    post("/supplementary-details")
                        .content(request)
                        .contentType("application/json")
                        .header("Authorization", USER_JWT_TOKEN)
                        .header("ServiceAuthorization", SERVICE_JWT_TOKEN)
                )
                .andExpect(resultMatcher)
                .andReturn();
            return response.getResponse().getContentAsString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // test will fail
            throw new RuntimeException(e);
        }
    }
}
