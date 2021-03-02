package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatethecase;

import static java.nio.charset.Charset.defaultCharset;
import static org.springframework.util.StreamUtils.copyToString;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public final class CaseWorkerRefDataMock {

    private static final String URL = "/refdata/case-worker/users/fetchUsersById";

    private CaseWorkerRefDataMock() {
        // we need it for the checkstyle check
    }

    public static void setup200MockResponse(WireMockServer mockService) throws IOException {
        mockService.stubFor(WireMock.post(WireMock.urlEqualTo(URL))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(
                    copyToString(
                        CaseWorkerRefDataMock.class.getClassLoader().getResourceAsStream("case-worker-ref-data-response.json"),
                        defaultCharset()))));
    }

    public static void setup404MockResponse(WireMockServer mockService) {
        mockService.stubFor(WireMock.post(WireMock.urlEqualTo(URL))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
            ));
    }
}
