package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Slf4j
@Component
public class GenerateServiceRequestMakeAPaymentConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");
        log.info("Test 12");
        return callback.getEvent() == Event.GENERATE_SERVICE_REQUEST;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        log.info("Test 13");
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        log.info("Test 14");
        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();
        log.info("Test 15");
        postSubmitResponse.setConfirmationHeader("# You have created a service request");
        postSubmitResponse.setConfirmationBody(
                "### What happens next\n\n"
                        + "You can now pay for this appeal in the 'Service Request' tab on the case details screen.\n\n"
                        + "[Service requests](cases/case-details/"
                        + callback.getCaseDetails().getId() + "#Service%20Request)\n\n"
        );
        log.info("Test 16");
        return postSubmitResponse;
    }

}
