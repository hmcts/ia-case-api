package uk.gov.hmcts.reform.iacaseapi.component.testutils.wiremock;

public class NotificationsApiCallbackTransformer extends CallbackTransformer {

    @Override
    public String getName() {
        return "ia-case-notifications-api-transformer";
    }
}
