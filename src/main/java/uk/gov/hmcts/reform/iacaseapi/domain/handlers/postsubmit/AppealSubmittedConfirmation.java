package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EA_HU_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUBMISSION_OUT_OF_TIME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
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

        final String paymentOptionPayOffline = "payOffline";
        final String paymentOptionPayLater = "payLater";
        final String whatHappensNextLabel = "#### What happens next\n\n";
        final String paPayAppealLabel = "You still have to pay for this appeal. You will soon receive a notification with instructions on how to pay by card online.";
        final String euHuPayAppealLabel = paPayAppealLabel + " You need to pay within 14 days of receiving the notification or the Tribunal will end the appeal.";

        final String paOverviewTabLabel = "[" + "overview tab" + "](/case/IA/Asylum/" + callback.getCaseDetails().getId() + "#overview)";
        final String paPayLaterLabel = "You still have to pay for this appeal. You can do this by selecting Make a payment from the dropdown on the " + paOverviewTabLabel + " and following the instructions.";

        YesOrNo submissionOutOfTime =
            requireNonNull(callback.getCaseDetails().getCaseData().read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)
                .<RequiredFieldMissingException>orElseThrow(() -> new RequiredFieldMissingException("submission out of time is a required field")));

        String paAppealTypePaymentOption = callback.getCaseDetails().getCaseData().read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");
        String eaHuAppealTypePaymentOption = callback.getCaseDetails().getCaseData().read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");

        if (submissionOutOfTime.equals(NO)) {
            postSubmitResponse.setConfirmationHeader("# Your appeal has been submitted");

            if (paAppealTypePaymentOption.equals(paymentOptionPayOffline)) {
                postSubmitResponse.setConfirmationBody(
                    whatHappensNextLabel
                    + paPayAppealLabel
                );

            } else if (paAppealTypePaymentOption.equals(paymentOptionPayLater)) {
                postSubmitResponse.setConfirmationBody(
                    whatHappensNextLabel
                    + paPayLaterLabel
                );

            } else if (eaHuAppealTypePaymentOption.equals(paymentOptionPayOffline)) {
                postSubmitResponse.setConfirmationBody(
                    whatHappensNextLabel
                    + euHuPayAppealLabel
                );
            } else {
                postSubmitResponse.setConfirmationBody(
                    whatHappensNextLabel
                    + "You will receive an email confirming that this appeal has been submitted successfully."
                );
            }

        } else {

            postSubmitResponse.setConfirmationHeader("");

            final String reviewLabel = "\n\nOnce you have paid for the appeal, a Tribunal Caseworker will review the reasons your appeal was out of time and you will be notified if it can proceed.";
            StringBuilder confirmationBody = new StringBuilder();
            confirmationBody.append(
                "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n"
                + whatHappensNextLabel
            );

            if (paAppealTypePaymentOption.equals(paymentOptionPayOffline)) {
                confirmationBody.append(paPayAppealLabel);
                confirmationBody.append(reviewLabel);
                postSubmitResponse.setConfirmationBody(confirmationBody.toString());

            } else if (paAppealTypePaymentOption.equals(paymentOptionPayLater)) {
                confirmationBody.append(paPayLaterLabel);
                confirmationBody.append(reviewLabel);
                postSubmitResponse.setConfirmationBody(confirmationBody.toString());

            } else if (eaHuAppealTypePaymentOption.equals(paymentOptionPayOffline)) {
                confirmationBody.append(euHuPayAppealLabel);
                confirmationBody.append(reviewLabel);
                postSubmitResponse.setConfirmationBody(confirmationBody.toString());

            } else {
                confirmationBody.append("You have submitted this appeal beyond the deadline. The Tribunal Case Officer will decide if it can proceed. You'll get an email telling you whether your appeal can go ahead.");
                postSubmitResponse.setConfirmationBody(confirmationBody.toString());
            }
        }

        return postSubmitResponse;
    }
}
