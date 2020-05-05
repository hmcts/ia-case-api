package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.CallbackForTest;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.PostSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.PreSubmitCallbackResponseForTest;

@Slf4j
public class IaCaseApiClient {

    private static final String SERVICE_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final String USER_JWT_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJkY28xbjRlNm43Mmo3Z3NyYTQxamhkdmQ0MCIsInN1YiI6IjIzIiwiaWF0IjoxNTYyODQ2NTYwLCJleHAiOjE1NjI4NTA1MDYsImRhdGEiOiJjYXNld29ya2VyLWlhLGNhc2V3b3JrZXItaWEtY2FzZW9mZmljZXIsY2FzZXdvcmtlcixjYXNld29ya2VyLWlhLWxvYTEsY2FzZXdvcmtlci1pYS1jYXNlb2ZmaWNlci1sb2ExLGNhc2V3b3JrZXItbG9hMSIsInR5cGUiOiJBQ0NFU1MiLCJpZCI6IjIzIiwiZm9yZW5hbWUiOiJDYXNlIiwic3VybmFtZSI6Ik9mZmljZXIiLCJkZWZhdWx0LXNlcnZpY2UiOiJDQ0QiLCJsb2EiOjEsImRlZmF1bHQtdXJsIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTAwMC9wb2MvY2NkIiwiZ3JvdXAiOiJjYXNld29ya2VyIn0.W_drH_9R5wchdR6ctMaCpgQNkfgSz5XtezODtsspG34";

    private final RestTemplate restTemplate;
    private final String aboutToSubmitUrl;
    private final String aboutToStartUrl;
    private final String ccdSubmittedUrl;

    public IaCaseApiClient(int port) {
        this.restTemplate = new RestTemplate();
        this.aboutToSubmitUrl = "http://localhost:" + port + "/asylum/ccdAboutToSubmit";
        this.aboutToStartUrl = "http://localhost:" + port + "/asylum/ccdAboutToStart";
        this.ccdSubmittedUrl = "http://localhost:" + port + "/asylum/ccdSubmitted";
    }

    public PreSubmitCallbackResponseForTest aboutToSubmit(CallbackForTest.CallbackForTestBuilder callback) {

        HttpEntity<CallbackForTest> requestEntity =
            new HttpEntity<>(callback.build(), getHeaders());

        ResponseEntity<PreSubmitCallbackResponseForTest> responseEntity =
            restTemplate.exchange(
                aboutToSubmitUrl,
                HttpMethod.POST,
                requestEntity,
                PreSubmitCallbackResponseForTest.class);

        return responseEntity.getBody();
    }

    public PreSubmitCallbackResponseForTest aboutToStart(CallbackForTest.CallbackForTestBuilder callback) {

        HttpEntity<CallbackForTest> requestEntity =
            new HttpEntity<>(callback.build(), getHeaders());

        ResponseEntity<PreSubmitCallbackResponseForTest> responseEntity =
            restTemplate.exchange(
                aboutToStartUrl,
                HttpMethod.POST,
                requestEntity,
                PreSubmitCallbackResponseForTest.class);

        return responseEntity.getBody();
    }

    public PostSubmitCallbackResponseForTest ccdSubmitted(CallbackForTest.CallbackForTestBuilder callback) {

        HttpEntity<CallbackForTest> requestEntity =
            new HttpEntity<>(callback.build(), getHeaders());

        ResponseEntity<PostSubmitCallbackResponseForTest> responseEntity =
            restTemplate.exchange(
                ccdSubmittedUrl,
                HttpMethod.POST,
                requestEntity,
                PostSubmitCallbackResponseForTest.class);

        return responseEntity.getBody();
    }

    private HttpHeaders getHeaders() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("ServiceAuthorization", SERVICE_JWT_TOKEN);
        headers.add("Authorization", USER_JWT_TOKEN);

        return headers;
    }
}
