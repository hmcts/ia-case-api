package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class UpdateInterpreterBookingStatusConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.UPDATE_INTERPRETER_BOOKING_STATUS;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        String updateInterpreterDetailsUrl =
                "/case/IA/Asylum/"
                        + callback.getCaseDetails().getId()
                        + "/trigger/updateInterpreterDetails";

        postSubmitResponse.setConfirmationHeader("# Booking statuses have been updated");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "The hearing has been updated with the interpreter booking status. This information is now visible in List Assist.<br><br>"
            + "Ensure that the [interpreter details](" + updateInterpreterDetailsUrl + ") are up to date."
        );

        return postSubmitResponse;
    }
}