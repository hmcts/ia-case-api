package uk.gov.hmcts.reform.iacaseapi.component.testutils.wiremock;

public class HomeOfficeIntegrationApiCallbackTransformer extends CallbackTransformer {

    @Override
    public String getName() {
        return "home-office-integration-api-transformer";
    }
}
