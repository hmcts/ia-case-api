package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.apache.http.HttpHeaders;
import org.springframework.http.HttpStatus;


public interface WithReferenceDataStub {

    default void addReferenceDataPrdResponseStub(WireMockServer server, String path, String responseJson) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.GET, urlEqualTo(path))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withHeader(HttpHeaders.CONNECTION, "close")
                    .withBody(responseJson)
                    .build()
            )
        );
    }

    default void addReferenceDataPrdOrganisationResponseStub(WireMockServer server, String path, String responseJson) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.GET, urlEqualTo(path))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withHeader(HttpHeaders.CONNECTION, "close")
                    .withBody(responseJson)
                    .build()
            )
        );
    }

    default void addReferenceCreatedStub(WireMockServer server, String path) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo("/ccd-data-store" + path))
                    .build(),
                aResponse()
                    .withStatus(HttpStatus.CREATED.value())
                    .withHeader("Content-Type", "application/json")
                    .build()
            )
        );
    }

}
