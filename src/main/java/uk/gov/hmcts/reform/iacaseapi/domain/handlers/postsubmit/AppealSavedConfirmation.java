package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class AppealSavedConfirmation implements PostSubmitCallbackHandler<CaseDataMap> {

    public boolean canHandle(
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.START_APPEAL || callback.getEvent() == Event.EDIT_APPEAL;
    }

    public PostSubmitCallbackResponse handle(
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        String submitAppealUrl =
            "/case/IA/Asylum/"
            + callback.getCaseDetails().getId()
            + "/trigger/submitAppeal";

        postSubmitResponse.setConfirmationHeader("# Appeal saved\nYou still need to submit it");
        postSubmitResponse.setConfirmationBody(
            "#### Ready to submit?\n\n"
            + "[Submit your appeal](" + submitAppealUrl + ") when you are ready.\n\n"
            + "#### Not ready to submit yet?\n"
            + "You can return to the case to make changes."
        );

        return postSubmitResponse;
    }
}
