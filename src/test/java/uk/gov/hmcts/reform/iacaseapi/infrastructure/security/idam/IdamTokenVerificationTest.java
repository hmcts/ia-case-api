package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.proc.JWSVerifierFactory;
import com.nimbusds.jose.util.IOUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class IdamTokenVerificationTest {

    @Mock RestTemplate restTemplate;

    private JWSVerifierFactory jwsVerifierFactory = new DefaultJWSVerifierFactory();
    private String baseUrl = "http://baseurl";
    private String jwksUri = "/jwks";
    private String validToken = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJpYS1jYXNlb2ZmaWNlckBmYWtlLmhtY3RzLm5ldCIsImF1dGhfbGV2ZWwiOjAsImF1ZGl0VHJhY2tpbmdJZCI6IjlkOWE5ODI2LTQyZDQtNDJkZC1iZjZiLTcyMTM5MGZjOWY0OSIsImlzcyI6Imh0dHA6Ly9mci1hbTo4MDgwL29wZW5hbS9vYXV0aDIvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiI2ZmI2YjhhYS1hZjc4LTQwN2MtYjgwZi04Y2JkNThkYTZiNWQiLCJhdWQiOiJjY2RfZ2F0ZXdheSIsIm5iZiI6MTU3OTE3MzI1MSwiZ3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyJdLCJhdXRoX3RpbWUiOjE1NzkxNzMyNTAwMDAsInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNTc5MjAyMDUxLCJpYXQiOjE1NzkxNzMyNTEsImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiIwM2Y1MmQzMC0yNjEzLTRmYjQtODU1Mi00Yjk0MWFlZDU4NjcifQ.n-pn4I1oaX0Uw0mw6PJMXKIlhHTwuMWFyfoatLYVNi3Sv-wYXFGmXE7ychsfXsZmKGvhBI5hZWQxnHHI4YjeJStmkV54Tkg7VwCfa8eD5TCabwiwo1We5Uut-2iC-6icixcPqCSnSJ6lI7sGjLvqzlUk8iGR7nISHIMicH77X5sM6sUjeqpnoqQqL3Vrlpeb9Ri7mTluKYVv9pKkp-ncsFkX86-ATrPXF_cyiRXPFYR0iwYXjx1J11ouVO16AoKlnwwt-ClGlCoC1xKTcFD32crP87Tw10Z6MdGwVSlBEB-cGJbMXuq8pQfB5BWe8eY1Y-CE1d4_Kqj0W6L3LPzzVA";
    private IdamTokenVerification idamTokenVerification;

    @Before
    public void setup() throws IOException {

        doReturn(new ResponseEntity<>(IOUtils.readInputStreamToString(getClass().getResourceAsStream("/idam-jwks.json"), StandardCharsets.UTF_8), HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                baseUrl + jwksUri,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class
            );

        idamTokenVerification = new IdamTokenVerification(baseUrl, jwksUri, restTemplate, jwsVerifierFactory);
    }

    @Test
    public void should_return_true_when_token_is_valid() {

        assertTrue(idamTokenVerification.verifyTokenSignature(validToken));
    }

    @Test
    public void should_return_false_when_cannot_parse_token() {

        assertFalse(idamTokenVerification.verifyTokenSignature("Bearer invalidToken"));
    }

    @Test
    public void should_return_false_when_cannot_load_jwks_keys() {

        doReturn(new ResponseEntity<>("", HttpStatus.OK))
            .when(restTemplate)
            .exchange(
                baseUrl + jwksUri,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class
            );

        assertFalse(idamTokenVerification.verifyTokenSignature(validToken));
    }

    @Test
    public void should_return_false_when_token_has_wrong_key() {

        String notExistingKeyToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJkY28xbjRlNm43Mmo3Z3NyYTQxamhkdmQ0MCIsInN1YiI6IjIzIiwiaWF0IjoxNTYyODQ2NTYwLCJleHAiOjE1NjI4NTA1MDYsImRhdGEiOiJjYXNld29ya2VyLWlhLGNhc2V3b3JrZXItaWEtY2FzZW9mZmljZXIsY2FzZXdvcmtlcixjYXNld29ya2VyLWlhLWxvYTEsY2FzZXdvcmtlci1pYS1jYXNlb2ZmaWNlci1sb2ExLGNhc2V3b3JrZXItbG9hMSIsInR5cGUiOiJBQ0NFU1MiLCJpZCI6IjIzIiwiZm9yZW5hbWUiOiJDYXNlIiwic3VybmFtZSI6Ik9mZmljZXIiLCJkZWZhdWx0LXNlcnZpY2UiOiJDQ0QiLCJsb2EiOjEsImRlZmF1bHQtdXJsIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTAwMC9wb2MvY2NkIiwiZ3JvdXAiOiJjYXNld29ya2VyIn0.W_drH_9R5wchdR6ctMaCpgQNkfgSz5XtezODtsspG34";

        assertFalse(idamTokenVerification.verifyTokenSignature(notExistingKeyToken));
    }

}