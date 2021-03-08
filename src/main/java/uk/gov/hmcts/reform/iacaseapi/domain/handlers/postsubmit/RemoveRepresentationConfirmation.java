package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdCaseAssignment;

@Slf4j
@Component
public class RemoveRepresentationConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final CcdCaseAssignment ccdCaseAssignment;

    public RemoveRepresentationConfirmation(CcdCaseAssignment ccdCaseAssignment) {
        this.ccdCaseAssignment = ccdCaseAssignment;
    }

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.REMOVE_REPRESENTATION;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        try {
            ccdCaseAssignment.applyNoc(callback);

            postSubmitResponse.setConfirmationHeader("# You have stopped representing this client");
            postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                + "We've sent you an email confirming you're no longer representing this client.\n"
                + "You have been removed from this case and no longer have access to it.\n\n"
                + "[View case list](/cases)"
            );
        } catch (Exception e) {
            log.error("Unable to remove representation (apply noc) for case id {} with error message: {}",
                callback.getCaseDetails().getId(), e.getMessage());

            postSubmitResponse.setConfirmationBody(
                "#### A problem has occurred\n\n"
                + "Unable to remove representation (apply noc) for case id "
                + callback.getCaseDetails().getId()
                + ".\n"
                + "Please contact your system administrator.\n\n"
                + "[View case list](/cases)"
            );
        }

        return postSubmitResponse;
    }
}
