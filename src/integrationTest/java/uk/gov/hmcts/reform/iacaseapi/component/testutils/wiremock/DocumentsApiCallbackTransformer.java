package uk.gov.hmcts.reform.iacaseapi.component.testutils.wiremock;

public class DocumentsApiCallbackTransformer extends CallbackTransformer {

    @Override
    public String getName() {
        return "ia-case-documents-api-transformer";
    }
}
