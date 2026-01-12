package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.IaCaseApiClient.USER_ID;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.IaCaseApiClient.USER_JWT_TOKEN;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public interface WithIdamStub {

    default void addIdamTokenStub(WireMockServer server) {
        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo("/userAuth/o/token"))
                    .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded; charset=UTF-8"))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"access_token\": \"" + USER_JWT_TOKEN + "\"}")
                    .build()
            )
        );
    }

    default void addUserInfoStub(WireMockServer server) {
        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.GET, urlEqualTo("/userAuth/o/userinfo"))
                    .withHeader("Authorization", equalTo("Bearer " + USER_JWT_TOKEN))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"uid\": \"" + USER_ID + "\"}")
                    .build()
            )
        );
    }
}
