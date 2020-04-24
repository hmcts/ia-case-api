package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUBMISSION_OUT_OF_TIME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class AppealSubmittedConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.SUBMIT_APPEAL;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();


        YesOrNo submissionOutOfTime =
                requireNonNull(callback.getCaseDetails().getCaseData().read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)
                        .<RequiredFieldMissingException>orElseThrow(() -> new RequiredFieldMissingException("submission out of time is a required field")));

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
                .orElseThrow(() -> new IllegalStateException("AppealType is not present"));

        if (submissionOutOfTime.equals(NO)) {

            postSubmitResponse.setConfirmationHeader("# Your appeal has been submitted");
            StringBuffer confirmationBody =
                    new StringBuffer("#### What happens next\n\n"
                            + "You will receive an email confirming that this appeal has been submitted successfully.");

            if (appealType.equals(AppealType.EA) || appealType.equals(AppealType.HU)) {
                confirmationBody.append("If there is any outstanding fee, you will need to pay this within 14 days.");
            }
            postSubmitResponse.setConfirmationBody(confirmationBody.toString());

        } else {

            postSubmitResponse.setConfirmationHeader("");
            postSubmitResponse.setConfirmationBody(
                    "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n"
                        + "## What happens Next\n\n"
                        + "You have submitted this appeal beyond the deadline.  The Tribunal Case Officer will decide if it can proceed. You'll get an email telling you whether your appeal can go ahead."
            );
        }

        return postSubmitResponse;
    }
}
