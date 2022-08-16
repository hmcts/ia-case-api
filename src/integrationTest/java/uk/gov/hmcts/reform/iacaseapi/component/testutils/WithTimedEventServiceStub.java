package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.springframework.http.HttpHeaders;

public interface WithTimedEventServiceStub {

    String expectedId = "someId";
    long caseId = 54321;

    default void addTimedEventServiceStub(WireMockServer server) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo("/timed-event-service/timed-event"))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withHeader(HttpHeaders.CONNECTION, "close")
                    .withBody("{ \"id\": \"" + expectedId + "\","
                        + " \"jurisdiction\": \"IA\","
                        + " \"caseType\": \"Asylum\","
                        + " \"caseId\": " + caseId + ","
                        + " \"event\": \"requestHearingRequirementsFeature\","
                        + " \"scheduledDateTime\": \"2020-05-13T10:00:00Z\" }")
                    .build()));
    }
}
