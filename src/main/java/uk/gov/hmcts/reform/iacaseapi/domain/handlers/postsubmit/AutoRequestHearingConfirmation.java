package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

public interface AutoRequestHearingConfirmation {

    String WHAT_HAPPENS_NEXT_LABEL = "#### What happens next\n\n";

    /*
    if isPanelRequired = Yes then no automatic hearing request
    If isPanelRequired != Yes then execute automatic hearing request (can be successful or failing)
     */
    default PostSubmitCallbackResponse buildAutoHearingRequestConfirmationResponse(long caseId,
                                                                                   boolean isPanelRequired,
                                                                                   boolean hearingRequestSuccessful,
                                                                                   String eventDescription) {

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        if (isPanelRequired) {
            postSubmitResponse.setConfirmationHeader("# " + eventDescription + " complete");
            postSubmitResponse.setConfirmationBody(WHAT_HAPPENS_NEXT_LABEL
                                                   + "The listing team will now list the case. All parties will be notified when "
                                                   + "the Hearing Notice is available to view");
        } else if (hearingRequestSuccessful) {
            postSubmitResponse.setConfirmationHeader("# Hearing listed");
            postSubmitResponse.setConfirmationBody(WHAT_HAPPENS_NEXT_LABEL
                                                   + "The hearing request has been created and is visible on the [Hearings tab]"
                                                   + "(/cases/case-details/" + caseId + "/hearings)");
        } else {
            postSubmitResponse.setConfirmationHeader("");
            postSubmitResponse.setConfirmationBody(
                "![Hearing could not be listed](https://raw.githubusercontent.com/hmcts/"
                + "ia-appeal-frontend/master/app/assets/images/hearingCouldNotBeListed.png)"
                + "\n\n"
                + WHAT_HAPPENS_NEXT_LABEL
                + "The hearing could not be auto-requested. Please manually request the "
                + "hearing via the [Hearings tab](/cases/case-details/" + caseId + "/hearings)");
        }

        return postSubmitResponse;
    }


}
