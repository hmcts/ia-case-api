package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.springframework.http.HttpHeaders;

public interface WithUserDetailsStub {

    default void addCaseWorkerUserDetailsStub(WireMockServer server) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.GET, urlEqualTo("/userAuth/o/userinfo"))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"sub\":\"someone@somewhere.com\","
                        + "\"uid\":\"1\",\"roles\":[\"caseworker-ia\",\"caseworker-ia-caseofficer\"],"
                        + "\"name\":null,\"given_name\":\"Case\",\"family_name\":\"Officer\"}")
                    .build()));

    }

    default void addLegalRepUserDetailsStub(WireMockServer server) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.GET, urlEqualTo("/userAuth/o/userinfo"))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withHeader(HttpHeaders.CONNECTION, "close")
                    .withBody("{\"sub\":\"someone@somewhere.com\","
                        + "\"uid\":\"1\",\"roles\":[\"caseworker-ia\",\"caseworker-ia-legalrep-solicitor\"],"
                        + "\"name\":null,\"given_name\":\"Legal rep\",\"family_name\":\"Solicitor\"}")
                    .build()));

    }

}
