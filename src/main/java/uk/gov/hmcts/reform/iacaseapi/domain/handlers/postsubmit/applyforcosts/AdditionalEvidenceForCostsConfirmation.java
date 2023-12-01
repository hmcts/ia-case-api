package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.applyforcosts;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class AdditionalEvidenceForCostsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.ADD_EVIDENCE_FOR_COSTS;
    }

    @Override
    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        String confirmationHeader = "# You've uploaded additional evidence'";
        String confirmationBody =
            "## What happens next\n\n"
                + "Both parties will receive an email notification confirming you've added additional evidence.\n\n"
                + "The Tribunal will consider the additional evidence as part of its review of the costs application.\n\n"
                + "You can review your costs application in the [Costs tab](/cases/case-details/" + callback.getCaseDetails().getId() + "#Costs). ";

        postSubmitResponse.setConfirmationHeader(confirmationHeader);
        postSubmitResponse.setConfirmationBody(confirmationBody);

        return postSubmitResponse;
    }
}
