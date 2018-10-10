package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PostSubmitCallbackHandler;

@Component
public class AppealSavedConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.SUBMITTED
               && (callback.getEventId() == EventId.START_APPEAL
                   || callback.getEventId() == EventId.CHANGE_APPEAL);
    }

    public PostSubmitCallbackResponse handle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        String changeAppealUrl =
            "/case/" + callback.getCaseDetails().getJurisdiction() + "/Asylum/" + callback.getCaseDetails().getId() + "/trigger/changeAppeal";

        String submitAppealUrl =
            "/case/" + callback.getCaseDetails().getJurisdiction() + "/Asylum/" + callback.getCaseDetails().getId() + "/trigger/submitAppeal";

        postSubmitResponse.setConfirmationHeader("# Now submit your appeal");
        postSubmitResponse.setConfirmationBody(
            "Now [submit your appeal](" + submitAppealUrl + ").\n\n"
            + "#### Not ready to submit yet?\n\n"
            + "You can return to the case details to [make changes](" + changeAppealUrl + ")"
        );

        return postSubmitResponse;
    }
}
