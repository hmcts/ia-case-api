package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REVIEW_TIME_EXTENSION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REVIEW_TIME_EXTENSION_DUE_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionDecision.GRANTED;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

import java.util.Optional;

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
        Optional<String> decision = asylumCase.read(REVIEW_TIME_EXTENSION_DECISION);


        if(decision.get().equals(GRANTED)) {
            postSubmitResponse.setConfirmationHeader("# You have been granted a time extension");
        } else {
            postSubmitResponse.setConfirmationHeader("# You have been rejected a time extension");
        }

        return postSubmitResponse;
    }
}

