package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.wiremock.DocumentsApiCallbackTransformer;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.AsylumCaseForTest;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.Builder;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.testutils.fixtures.UserDetailsForTest;

public class GivensBuilder {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DocumentsApiCallbackTransformer documentsApiCallbackTransformer;

    public GivensBuilder(DocumentsApiCallbackTransformer documentsApiCallbackTransformer) {
        this.documentsApiCallbackTransformer = documentsApiCallbackTransformer;
    }

    public GivensBuilder someLoggedIn(UserDetailsForTest.UserDetailsForTestBuilder userDetailsForTestBuilder) {

        stubFor(get(urlEqualTo("/userAuth/details"))
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
