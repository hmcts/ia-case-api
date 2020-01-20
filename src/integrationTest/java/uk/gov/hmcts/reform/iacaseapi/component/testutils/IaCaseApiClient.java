package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PostSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;

@Slf4j
public class IaCaseApiClient {

    private static final String SERVICE_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final String USER_JWT_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJpYS1jYXNlb2ZmaWNlckBmYWtlLmhtY3RzLm5ldCIsImF1dGhfbGV2ZWwiOjAsImF1ZGl0VHJhY2tpbmdJZCI6IjlkOWE5ODI2LTQyZDQtNDJkZC1iZjZiLTcyMTM5MGZjOWY0OSIsImlzcyI6Imh0dHA6Ly9mci1hbTo4MDgwL29wZW5hbS9vYXV0aDIvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiI2ZmI2YjhhYS1hZjc4LTQwN2MtYjgwZi04Y2JkNThkYTZiNWQiLCJhdWQiOiJjY2RfZ2F0ZXdheSIsIm5iZiI6MTU3OTE3MzI1MSwiZ3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyJdLCJhdXRoX3RpbWUiOjE1NzkxNzMyNTAwMDAsInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNTc5MjAyMDUxLCJpYXQiOjE1NzkxNzMyNTEsImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiIwM2Y1MmQzMC0yNjEzLTRmYjQtODU1Mi00Yjk0MWFlZDU4NjcifQ.n-pn4I1oaX0Uw0mw6PJMXKIlhHTwuMWFyfoatLYVNi3Sv-wYXFGmXE7ychsfXsZmKGvhBI5hZWQxnHHI4YjeJStmkV54Tkg7VwCfa8eD5TCabwiwo1We5Uut-2iC-6icixcPqCSnSJ6lI7sGjLvqzlUk8iGR7nISHIMicH77X5sM6sUjeqpnoqQqL3Vrlpeb9Ri7mTluKYVv9pKkp-ncsFkX86-ATrPXF_cyiRXPFYR0iwYXjx1J11ouVO16AoKlnwwt-ClGlCoC1xKTcFD32crP87Tw10Z6MdGwVSlBEB-cGJbMXuq8pQfB5BWe8eY1Y-CE1d4_Kqj0W6L3LPzzVA";

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