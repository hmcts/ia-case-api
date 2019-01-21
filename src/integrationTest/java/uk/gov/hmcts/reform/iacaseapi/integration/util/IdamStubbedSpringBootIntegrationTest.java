package uk.gov.hmcts.reform.iacaseapi.integration.util;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.Before;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.RequestUserAccessTokenProvider;

@TestPropertySource(properties = {
        "idam.s2s-auth.url=http://127.0.0.1:8990",
        "auth.idam.client.baseUrl=http://127.0.0.1:8990"})
public abstract class IdamStubbedSpringBootIntegrationTest extends SpringBootIntegrationTest {

    private ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private RequestUserAccessTokenProvider requestUserAccessTokenProvider;

    @Before
    public void stubRequestUserCredentials() {

        when(requestUserAccessTokenProvider.tryGetAccessToken())
            .thenReturn(Optional.of("eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxMjM0Iiwic3ViIjoiMSIsImlhd"
                                    + "CI6MTU0ODA3NzQwMiwiZXhwIjoxNjQ4MTA2MjAyLCJkYXRhIjoiY2FzZXd"
                                    + "vcmtlci1pYSxjYXNld29ya2VyLWlhLWxlZ2FscmVwLXNvbGljaXRvciIsI"
                                    + "nR5cGUiOiJBQ0NFU1MiLCJpZCI6IjEiLCJmb3JlbmFtZSI6IlRlc3QiLCJ"
                                    + "zdXJuYW1lIjoiVG9rZW4ifQ.SKNPcMM2XWgfApxDeKkUy63Zv-z-aa5Cw1"
                                    + "wE2LgcZr0"));
    }

    @Before
    public void setupIdamStubs() throws Exception {

        stubFor(post(urlEqualTo("/oauth2/authorize"))
                .withHeader("Accept", equalTo("application/json, application/json, application/*+json, application/*+json"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded;charset=UTF-8"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(singletonMap("code", "some-auth-code")))));

        stubFor(post(urlEqualTo("/oauth2/token"))
                .withHeader("Accept", equalTo("application/json, application/json, application/*+json, application/*+json"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded;charset=UTF-8"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(singletonMap("access_token", someJwtToken())))));

        stubFor(post(urlEqualTo("/lease"))
                .withHeader("Accept", equalTo("text/plain"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(someJwtToken())));
    }

    private String someJwtToken() {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJpZCI6MX0.nAdWVTMzg4nt_7mBFbz9DVkHqmwW2qwSiXb7EJZjPSk";
    }
}
