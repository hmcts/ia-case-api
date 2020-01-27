package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import org.springframework.http.MediaType;

public class DocumentCaseApiVerifications {

    public void theDocumentsApiReceivesACcdAboutToSubmitCallback() {

        verify(
            postRequestedFor(urlEqualTo("/ia-case-documents-api/asylum/ccdAboutToSubmit"))
                .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON_VALUE)));
    }
}
