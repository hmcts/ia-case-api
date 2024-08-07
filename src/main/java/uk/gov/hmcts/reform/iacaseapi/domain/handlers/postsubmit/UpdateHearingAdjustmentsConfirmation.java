package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_ADJUSTMENTS;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;


@Component
public class UpdateHearingAdjustmentsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return UPDATE_HEARING_ADJUSTMENTS == callback.getEvent();
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You've recorded the agreed hearing adjustments");
        StringBuilder messageContentString = new StringBuilder("#### What happens next\n\n");

        String addCaseFlagUrl =
            "/case/IA/Asylum/"
            + callback.getCaseDetails().getId()
            + "/trigger/createFlag";

        String manageCaseFlagUrl =
            "/case/IA/Asylum/"
            + callback.getCaseDetails().getId()
            + "/trigger/manageFlags";

        messageContentString.append("You should ensure that the case flags reflect the hearing requests that have been approved. This may require adding new case flags or making active flags inactive.\n\n"
                                    + "[Add case flag](" + addCaseFlagUrl + ")<br>"
                                    + "[Manage case flags](" + manageCaseFlagUrl + ")<br><br>");

        messageContentString.append("The listing team will now list the case. All parties will be notified when the Hearing Notice is available to view.<br><br>");
        postSubmitResponse.setConfirmationBody(messageContentString.toString());

        return postSubmitResponse;
    }
}
