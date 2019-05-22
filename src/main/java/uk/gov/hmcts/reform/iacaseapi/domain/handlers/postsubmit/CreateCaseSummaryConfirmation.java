package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class CreateCaseSummaryConfirmation implements PostSubmitCallbackHandler<CaseDataMap> {

    public boolean canHandle(
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.CREATE_CASE_SUMMARY;
    }

    public PostSubmitCallbackResponse handle(
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You have uploaded the case summary");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next?\n\n"
            + "Generate the hearing ready bundle if you are ready to do so."
        );

        return postSubmitResponse;
    }
}
