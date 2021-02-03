package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public interface WithAcaAssignmentsStub {

    String expectedAssigneeId = "someId";
    long expectedCaseId = 9999L;

    default void addAcaAssignmentsStub(WireMockServer server) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo("/case-assignments"))
                    .build(),
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{ \"assignee_id\": \"" + expectedAssigneeId + "\","
                              + " \"case_id\": \"" + expectedCaseId + "\","
                              + " \"case_type\": \"Asylum\" }")
                    .build()));
    }
}
