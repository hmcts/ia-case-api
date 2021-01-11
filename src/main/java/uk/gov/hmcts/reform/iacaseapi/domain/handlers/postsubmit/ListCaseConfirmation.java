package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class ListCaseConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.LIST_CASE;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final String hoRequestEvidenceInstructStatus =
            asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_HEARING_INSTRUCT_STATUS, String.class).orElse("");

        PostSubmitCallbackResponse postSubmitResponse = new PostSubmitCallbackResponse();

        if (hoRequestEvidenceInstructStatus.equalsIgnoreCase("FAIL")) {
            postSubmitResponse.setConfirmationBody(
                "![Respondent notification failed confirmation]"
                + "(https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/respondent_notification_failed.svg)\n"
                + "#### Do this next\n\n"
                + "Contact the respondent to tell them what has changed, including any action they need to take.\n"
            );
        } else {

            postSubmitResponse.setConfirmationHeader("# You have listed the case");
            postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                + "The hearing notice will be sent to all parties.<br>"
                + "You don't need to do any more on this case."
            );
        }


        return postSubmitResponse;
    }
}
