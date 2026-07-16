package uk.gov.hmcts.reform.iacaseapi.component.testutils.wiremock;

public class PaymentApiCallbackTransformer extends CallbackTransformer {

    @Override
    public String getName() {
        return "ia-case-payments-api-transformer";
    }
}
