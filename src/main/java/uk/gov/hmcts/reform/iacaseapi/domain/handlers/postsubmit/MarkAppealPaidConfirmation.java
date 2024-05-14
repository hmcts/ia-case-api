package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class MarkAppealPaidConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");
        
        return callback.getEvent() == Event.MARK_APPEAL_PAID;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# Your have marked the appeal as paid");
        if (isInternalCase(callback.getCaseDetails().getCaseData())) {
            postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                + "The appellant has been notified that the fee has been paid. The appeal will progress as usual."
            );
        } else {
            postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                            + "The Tribunal will be notified that the fee has been paid. The appeal will progress as usual."
            );
        }

        return postSubmitResponse;
    }
}
