package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class GenerateServiceRequestMakeAPaymentConfirmation implements PostSubmitCallbackHandler<AsylumCase> {


    public GenerateServiceRequestMakeAPaymentConfirmation() {
    }

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.GENERATE_SERVICE_REQUEST;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You have generated a service request");
        postSubmitResponse.setConfirmationBody(
                "### Do this next\n\n"
                        + "You need to go to the service request tab to pay for your appeal.\n\n"
                        + "[Service Requests](cases/case-details/"
                        + callback.getCaseDetails().getId() + "#Service%20Request)\n\n"
        );

        return postSubmitResponse;
    }

}
