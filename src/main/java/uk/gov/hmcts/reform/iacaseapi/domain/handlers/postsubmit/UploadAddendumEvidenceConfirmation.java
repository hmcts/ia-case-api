package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class UploadAddendumEvidenceConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE
               || callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP
               || callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE
               || callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You have uploaded\n# additional evidence");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "The evidence is now available in the documents tab. This is not included in the hearing bundle, it is added as an addendum."
        );

        return postSubmitResponse;
    }
}
