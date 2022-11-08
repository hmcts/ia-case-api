package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class RemoveAppealFromOnlineConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.REMOVE_APPEAL_FROM_ONLINE;
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<AsylumCase> callback) {

        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You've removed this appeal from the online service");
        postSubmitResponse.setConfirmationBody(
            "## Do this next\n"
            + "You now need to:</br>"
            + "1.Contact the appellant and the respondent to inform them that the case will proceed offline.</br>"
            + "2.Save all files associated with the appeal to the shared drive.</br>"
            + "3.Email a link to the saved files with the appeal reference number to: BAUArnhemHouse@justice.gov.uk"
        );

        return postSubmitResponse;
    }
}
