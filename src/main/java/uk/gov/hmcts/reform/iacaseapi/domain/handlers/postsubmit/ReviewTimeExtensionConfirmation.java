package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REVIEW_TIME_EXTENSION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionDecision.GRANTED;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class ReviewTimeExtensionConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.REVIEW_TIME_EXTENSION;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        TimeExtensionDecision decision = getTimeExtensionDecision(asylumCase);

        String confirmationHeader = decision == GRANTED
            ? "# You have granted a time extension"
            : "# You have refused a time extension";

        postSubmitResponse.setConfirmationHeader(confirmationHeader);


        //TODO: Next step Direction name should be dynamic too.
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "The appellant has been notified that their request has been "
            + decision.toString()
            + " and that they must submit their Appeal Reasons by the new due date.<br>"
            + "You will be notified when it is ready to review.\n"
        );
        return postSubmitResponse;
    }

    private TimeExtensionDecision getTimeExtensionDecision(AsylumCase asylumCase) {
        Optional<TimeExtensionDecision> read = asylumCase.read(REVIEW_TIME_EXTENSION_DECISION);
        return read.orElseThrow(() -> new IllegalStateException("Cannot handle " + Event.REVIEW_TIME_EXTENSION + " without a decision"));
    }
}

