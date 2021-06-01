package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class GenerateUpperTribunalBundleConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.GENERATE_UPPER_TRIBUNAL_BUNDLE;
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<AsylumCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse = new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# The Upper Tribunal bundle is being generated");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "You will soon be able to view and download the bundle under Upper Tribunal documents in the "
            + "[documents tab](/case/IA/Asylum/" + callback.getCaseDetails().getId() + "/#Documents)."
            + "\n\n\n"
            + "If the bundle fails to generate, you will be notified and must follow the same steps to generate the bundle again."
        );

        return postSubmitResponse;
    }
}
