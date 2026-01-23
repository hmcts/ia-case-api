package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;

public interface WithCcdStub {

    String expectedCaseId = "9999";
    String expectedCaseRole = "[CREATOR]";
    String expectedOrganisationId = "259facf";
    String expectedUserId = "1";

    default void addCcdAssignmentsStub(WireMockServer server) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.DELETE, urlEqualTo("/ccd-data-store/case-users"))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{ \"case_id\": \"" + expectedCaseId + "\","
                              + " \"case_role\": \"" + expectedCaseRole + "\","
                              + " \"organisation_id\": \"" + expectedOrganisationId + "\","
                              + " \"user_id\": " + expectedUserId + "}")
                    .build()));
    }


    default void addSearchStub(WireMockServer server, Resource resourceFile) throws IOException {

        String ccdDataResponseJson =
            new String(Files.readAllBytes(Paths.get(resourceFile.getURI())));

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo("/ccd-data-store/searchCases?ctid=Asylum"))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(ccdDataResponseJson)
                    .build()
            )
        );
    }
}
