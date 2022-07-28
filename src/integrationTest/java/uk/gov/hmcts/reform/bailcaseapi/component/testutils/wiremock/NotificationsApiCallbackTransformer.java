package uk.gov.hmcts.reform.bailcaseapi.component.testutils.wiremock;

public class NotificationsApiCallbackTransformer extends CallbackTransformer {

    @Override
    public String getName() {
        return "ia-case-notifications-api-transformer";
    }
}
