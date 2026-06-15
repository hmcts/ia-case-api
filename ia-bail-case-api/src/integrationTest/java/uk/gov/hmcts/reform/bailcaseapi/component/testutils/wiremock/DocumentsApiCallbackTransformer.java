package uk.gov.hmcts.reform.bailcaseapi.component.testutils.wiremock;

public class DocumentsApiCallbackTransformer extends CallbackTransformer {

    @Override
    public String getName() {
        return "ia-case-documents-api-transformer";
    }
}
