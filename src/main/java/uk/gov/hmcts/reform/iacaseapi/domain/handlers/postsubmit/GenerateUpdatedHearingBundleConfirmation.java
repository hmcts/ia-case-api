package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;

@Component
public class GenerateUpdatedHearingBundleConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.GENERATE_UPDATED_HEARING_BUNDLE;
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<AsylumCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse = new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# The hearing bundle is being generated");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "You will soon be able to view the hearing bundle in the [Documents tab](/cases/case-details/1234#Documents) in your case details page.</br>"
            + "All other parties will be notified when the hearing bundle is available.</br>"
            + "If the bundle fails to generate, you will get a notification and you will need to try again."
        );

        return postSubmitResponse;
    }

}
