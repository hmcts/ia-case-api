package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class BuildCaseConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.BUILD_CASE;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        String submitCaseUrl =
            "/case/IA/Asylum/"
            + callback.getCaseDetails().getId()
            + "/trigger/submitCase";

        String buildCaseUrl =
            "/case/IA/Asylum/"
            + callback.getCaseDetails().getId()
            + "/trigger/buildCase";

        postSubmitResponse.setConfirmationHeader("# Upload saved\nYou still need to submit your case");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next?\n\n"
            + "If you're ready for your case to be reviewed, [submit your case](" + submitCaseUrl + ").\n\n"
            + "If you're not yet ready for your case to be reviewed, continue to [build your case](" + buildCaseUrl + ")."
        );

        return postSubmitResponse;
    }
}
