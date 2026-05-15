package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class UploadHomeOfficeAppealResponseConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;

    public UploadHomeOfficeAppealResponseConfirmation(
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

        return callback.getEvent() == Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse = new PostSubmitCallbackResponse();
        postSubmitResponse.setConfirmationHeader("# You've uploaded the appeal response");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        boolean is24WeekCase = asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)
            .map(flag -> flag == YesOrNo.YES)
            .orElse(false);

        if (is24WeekCase) {
            UserRoleLabel role = userDetailsHelper.getLoggedInUserRoleLabel(userDetails);

            if (role == UserRoleLabel.TRIBUNAL_CASEWORKER || role == UserRoleLabel.ADMIN_OFFICER) {
                postSubmitResponse.setConfirmationBody(
                    "#### Do this next\n\n"
                        + "Check the response uploaded by the respondent.\n\n"
                        + "If it complies with the Procedure Rules and Practice Directions and this case has a decision with a hearing, "
                        + "select [Force case - Prepare for hearing](/case/IA/Asylum/"
                        + callback.getCaseDetails().getId() + "/trigger/forceCaseToPrepareForHearing).\n\n"
                        + "If this case has a decision without a hearing then "
                        + "select [Decision without a hearing](/case/IA/Asylum/"
                        + callback.getCaseDetails().getId() + "/trigger/decisionWithoutHearing).\n\n"
                        + "If it does not comply direct the respondent to make the appropriate changes."
                );
            } else if (role == UserRoleLabel.LEGAL_REPRESENTATIVE) {
                postSubmitResponse.setConfirmationBody(
                    "#### Do this next\n\n"
                        + "The case has now been sent to the respondent for review.\n\n"
                        + "When they've reviewed the case, we'll add their response to the documents tab. "
                        + "You'll get an email letting you know when it's there."
                );
            } else {
                postSubmitResponse.setConfirmationBody(
                    "#### Do this next\n\n"
                        + "The Tribunal will: \n* check that the Home Office response complies with the Procedure Rules and Practice Directions\n* inform you of any issues\n\n"
                        + "Providing there are no issues, the response will be shared with the appellant."
                );
            }
        } else {
            postSubmitResponse.setConfirmationBody(
                "#### Do this next\n\n"
                    + "The Tribunal will: \n* check that the Home Office response complies with the Procedure Rules and Practice Directions\n* inform you of any issues\n\n"
                    + "Providing there are no issues, the response will be shared with the appellant."
            );
        }

        return postSubmitResponse;
    }
}
