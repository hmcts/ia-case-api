package uk.gov.hmcts.reform.iacaseapi;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"S2S_URL=http://127.0.0.1:8990"})
public abstract class Service2ServiceEnabledIntegrationTest extends SpringBootIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8990);

    @Before
    public void setupIdamStubs() throws Exception {

        stubFor(get(urlEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("ia")));
    }
}
