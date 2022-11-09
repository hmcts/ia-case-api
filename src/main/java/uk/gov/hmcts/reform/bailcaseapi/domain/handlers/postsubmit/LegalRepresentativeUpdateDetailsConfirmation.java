package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;


@Component
public class LegalRepresentativeUpdateDetailsConfirmation implements PostSubmitCallbackHandler<BailCase> {

    public boolean canHandle(
        Callback<BailCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.UPDATE_BAIL_LEGAL_REP_DETAILS;
    }

    public PostSubmitCallbackResponse handle(
        Callback<BailCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You've updated the legal representative's details");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\nThe service will be updated. The new details will be used on all "
                + "future correspondence and documents.<br />"
        );

        return postSubmitResponse;
    }
}
