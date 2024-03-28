package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class GenerateServiceRequestMakeAPaymentConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;

    public GenerateServiceRequestMakeAPaymentConfirmation(
        UserDetails userDetails,
        UserDetailsHelper userDetailsHelper
    ) {
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
    }

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.GENERATE_SERVICE_REQUEST;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();
        UserRoleLabel currentUser = userDetailsHelper.getLoggedInUserRoleLabel(userDetails);

        postSubmitResponse.setConfirmationHeader("# You have created a service request");
        if (currentUser == UserRoleLabel.ADMIN_OFFICER) {
            postSubmitResponse.setConfirmationBody(
                "### What happens next\n\n"
                    + "The legal representative can now pay for this appeal in the 'Service Request' tab on the case details screen.\n\n"
            );
        } else {
            postSubmitResponse.setConfirmationBody(
                "### What happens next\n\n"
                    + "You can now pay for this appeal in the 'Service Request' tab on the case details screen.\n\n"
                    + "[Service requests](cases/case-details/"
                    + callback.getCaseDetails().getId() + "#Service%20Request)\n\n"
            );
        }

        return postSubmitResponse;
    }

}
