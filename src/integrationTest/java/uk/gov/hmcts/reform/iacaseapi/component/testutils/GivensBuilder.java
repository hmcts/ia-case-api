package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.Builder;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.UserDetailsForTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.wiremock.DocumentsApiCallbackTransformer;

public class GivensBuilder {

    private final Resource resourceJwksFile;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DocumentsApiCallbackTransformer documentsApiCallbackTransformer;

    public GivensBuilder(DocumentsApiCallbackTransformer documentsApiCallbackTransformer, Resource resourceJwksFile) {
        this.resourceJwksFile = resourceJwksFile;
        this.documentsApiCallbackTransformer = documentsApiCallbackTransformer;
    }

    public GivensBuilder someLoggedIn(UserDetailsForTest.UserDetailsForTestBuilder userDetailsForTestBuilder) {

        String jwksResponse = "";
        try {
            jwksResponse = FileUtils.readFileToString(resourceJwksFile.getFile());
        } catch (IOException e) {
            // ignore this
        }

        stubFor(get(urlEqualTo("/userAuth/o/jwks"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(jwksResponse)));

        stubFor(get(urlEqualTo("/userAuth/o/userinfo"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(
                    getObjectAsJsonString(
                        userDetailsForTestBuilder))));

        return this;
    }

    public GivensBuilder theDocumentsApiWillRespondWith(AsylumCaseForTest additionalAsylumCaseValues) {

        additionalAsylumCaseValues.build()
            .forEach(documentsApiCallbackTransformer::addAdditionalAsylumCaseData);

        return theDocumentsApiWillRespondWithoutAdditionalCaseData();
    }

    public GivensBuilder theDocumentsApiWillRespondWithoutAdditionalCaseData() {

        stubFor(post(urlEqualTo("/ia-case-documents-api/asylum/ccdAboutToSubmit"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withTransformers("ia-case-documents-api-transformer")));

        return this;
    }

    private String getObjectAsJsonString(Builder builder) {

        try {
            return objectMapper.writeValueAsString(builder.build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Couldn't serialize object", e);
        }
    }

    public GivensBuilder and() {
        return this;
    }
}
