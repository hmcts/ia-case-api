package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Slf4j
@Component
public class AddCaseNoteConfirmation implements PostSubmitCallbackHandler<AsylumCase> {
    
    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");
        log.info("CanHandle 1 AddCaseNoteConfirmation: {}", callback.getEvent());

        return callback.getEvent() == Event.ADD_CASE_NOTE;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        log.info("PostSubmitCallbackResponse AddCaseNoteConfirmation for event: {}", callback.getEvent());
        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You have added a case note");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "You can review this note in the case notes tab."
        );

        return postSubmitResponse;
    }
}
