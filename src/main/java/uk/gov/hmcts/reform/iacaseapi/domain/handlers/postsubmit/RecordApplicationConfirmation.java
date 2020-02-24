package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationDecision.GRANTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATION_TYPE;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class RecordApplicationConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.RECORD_APPLICATION;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        String applicationDecision = asylumCase
            .read(APPLICATION_DECISION, String.class)
            .orElseThrow(() -> new IllegalStateException("applicationDecision is not present"));

        String applicationType = asylumCase
            .read(APPLICATION_TYPE, String.class)
            .orElseThrow(() -> new IllegalStateException("applicationType is not present"));

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You have recorded an application");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + getMessageAction(applicationDecision, applicationType, callback.getCaseDetails().getId())
        );

        asylumCase.clear(APPLICATION_DECISION);
        asylumCase.clear(APPLICATION_TYPE);

        return postSubmitResponse;
    }

    private String getMessageAction(String decision, String type, long id) {

        final String defaultResponse = "The application decision has been recorded and is now available in the applications tab. ";

        if (GRANTED.toString().equalsIgnoreCase(decision)) {

            if (TIME_EXTENSION.toString().equalsIgnoreCase(type)) {
                return "You must now [change the direction due date](/case/IA/Asylum/" + id + "/trigger/changeDirectionDueDate). You can also view the application decision in the Applications tab.";

            } else if (WITHDRAW.toString().equalsIgnoreCase(type)) {
                return defaultResponse + "You must now [end the appeal](/case/IA/Asylum/" + id +  "/trigger/endAppeal).";

            } else if (ADJOURN.toString().equalsIgnoreCase(type) || EXPEDITE.toString().equalsIgnoreCase(type) || TRANSFER.toString().equalsIgnoreCase(type)) {
                return defaultResponse + "Contact the listing team to relist the case. Once the case has been relisted, a new hearing notice will be issued.";
            } else if (UPDATE_HEARING_REQUIREMENTS.toString().equalsIgnoreCase(type)) {
                return "You must now [update the hearing requirements](/case/IA/Asylum/" + id +  "/trigger/updateHearingRequirements) based on the new information provided in the application. The application decision is available to view in the Application tab.";
            }

            // default empty in case of new application type
            return "";

        } else {
            return defaultResponse + "A notification will be sent to both parties, informing them that an application was requested and refused. The case will progress as usual.";
        }
    }
}
