package uk.gov.hmcts.reform.bailcaseapi.component.testutils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.CallbackForTest;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.PostSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;

@Slf4j
public class IaBailCaseApiClient {

    private static final String SERVICE_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NT"
        + "Y3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final String USER_JWT_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3k"
        + "rV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJjY2QtaW1wb3J0QGZha2UuaG1jdHMubmV0IiwiYXV0aF9sZXZlbC"
        + "I6MCwiYXVkaXRUcmFja2luZ0lkIjoiZDg3ODI3ODQtMWU0NC00NjkyLTg0NzgtNTI5MzE0NTVhNGI5IiwiaXNzIjoiaHR0cDovL2ZyLWFt"
        + "OjgwODAvb3BlbmFtL29hdXRoMi9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdX"
        + "RoR3JhbnRJZCI6IjNjMWMzNjFkLTRlYzUtNGY0NS1iYzI0LTUxOGMzMDk0MzUxYiIsImF1ZCI6ImNjZF9nYXRld2F5IiwibmJmIjoxNTg"
        + "0NTI2MzcyLCJncmFudF90eXBlIjoicGFzc3dvcmQiLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiXSwiYXV0aF90aW1"
        + "lIjoxNTg0NTI2MzcyLCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTU4NDU1NTE3MiwiaWF0IjoxNTg0NTI2MzcyLCJleHBpcmVzX2luIjo"
        + "yODgwMCwianRpIjoiNDhjNDMzYTQtZmRiNS00YTIwLWFmNGUtMmYyNjIyYjYzZmU5In0.WP8ATcHMmdtG2W443aqNz3ES6-Bqng0IKjT"
        + "QfbndN1HrBLJWJtpC3qfzy2wD_CdiPU4uspdN5S91nhiT8Ub6DjstnDz3VPmR3Cbdk5QJBdAsQ0ah9w-duS8SA_dlzDIMt18bSDMUUd"
        + "ck6YxsoNyQFisI6cKNnfgB9ZTLhenVENtdmyrKVr96Ezp-jhhzmMVMxb1rW7KghSAH0ZCsWqlhrM--jPGRCweDiFe-ldi4EuhIxGbk"
        + "PjyWwsdcgmYfIuFrSxqV0vrSI37DNZx_Sh5DVJpUgSrYKRzuMqe4rFN6WVyHIY_Qu52ER2yrNYtGbAQ5AyMabPTPj9VVxqpa5nYUAg";

    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    private final String aboutToSubmitUrl;
    private final String aboutToStartUrl;
    private final String ccdSubmittedUrl;

    public IaBailCaseApiClient(ObjectMapper objectMapper, MockMvc mockMvc) {
        this.objectMapper = objectMapper;
        this.mockMvc = mockMvc;
        this.aboutToSubmitUrl = "/bail/ccdAboutToSubmit";
        this.aboutToStartUrl = "/bail/ccdAboutToStart";
        this.ccdSubmittedUrl = "/bail/ccdSubmitted";
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
}
