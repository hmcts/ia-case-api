package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;

@Component
public class UpdateInterpreterDetailsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private LocationBasedFeatureToggler locationBasedFeatureToggler;

    public UpdateInterpreterDetailsConfirmation(LocationBasedFeatureToggler locationBasedFeatureToggler) {
        this.locationBasedFeatureToggler = locationBasedFeatureToggler;
    }

    public boolean canHandle(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.UPDATE_INTERPRETER_DETAILS;
    }

    public PostSubmitCallbackResponse handle(Callback<AsylumCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String messageBody;
        if (locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase) == NO) {
            String hearingsTabUrl = "/case/IA/Asylum/" + callback.getCaseDetails().getId() + "#Hearing%20and%20appointment";

            messageBody = "You now need to update the hearing in the "
                    + "[Hearings tab](" + hearingsTabUrl + ")"
                    + " to ensure the new interpreter information is displayed in List Assist."
                    + "\n\nIf updates need to be made to the interpreter booking status this should be completed"
                    + " before updating the hearing.";
        } else {
            String updateInterpreterStatusUrl =
                    "/case/IA/Asylum/"
                            + callback.getCaseDetails().getId()
                            + "/trigger/updateInterpreterBookingStatus";

            messageBody = "The hearing has been updated with the interpreter details. This information is now visible in List Assist.<br><br>"
                    + "Ensure that the [interpreter booking status](" + updateInterpreterStatusUrl + ") is up to date.";
        }

        PostSubmitCallbackResponse postSubmitResponse = new PostSubmitCallbackResponse();



        postSubmitResponse.setConfirmationHeader("# Interpreter details have been updated");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + messageBody
        );

        return postSubmitResponse;
    }
}