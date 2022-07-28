package uk.gov.hmcts.reform.bailcaseapi.component.testutils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

public interface WithUserDetailsStub {

    default void addAdminOfficerUserDetailsStub(WireMockServer server) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.GET, urlEqualTo("/userAuth/o/userinfo"))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"sub\":\"someone@somewhere.com\","
                        + "\"uid\":\"1\",\"roles\":[\"caseworker-ia\",\"caseworker-ia-admofficer\"],"
                        + "\"name\":null,\"given_name\":\"Admin\",\"family_name\":\"Officer\"}")
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
                    .withBody("{\"sub\":\"someone@somewhere.com\","
                        + "\"uid\":\"1\",\"roles\":[\"caseworker-ia\",\"caseworker-ia-legalrep-solicitor\"],"
                        + "\"name\":null,\"given_name\":\"Legal rep\",\"family_name\":\"Solicitor\"}")
                    .build()));

    }

}
