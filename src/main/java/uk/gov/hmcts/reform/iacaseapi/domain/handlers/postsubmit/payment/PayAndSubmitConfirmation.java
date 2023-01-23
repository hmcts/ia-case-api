package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.payment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import java.time.ZoneId;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.AppealPaymentConfirmationProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PostNotificationSender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdSupplementaryUpdater;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@Slf4j
@Component
public class PayAndSubmitConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final AppealPaymentConfirmationProvider appealPaymentConfirmationProvider;
    private final FeePayment<AsylumCase> feePayment;
    private final PostNotificationSender<AsylumCase> postNotificationSender;
    private final Scheduler scheduler;
    private final DateProvider dateProvider;
    private final CcdSupplementaryUpdater ccdSupplementaryUpdater;

    public PayAndSubmitConfirmation(
        AppealPaymentConfirmationProvider appealPaymentConfirmationProvider,
        FeePayment<AsylumCase> feePayment,
        PostNotificationSender<AsylumCase> postNotificationSender,
        Scheduler scheduler,
        DateProvider dateProvider,
        CcdSupplementaryUpdater ccdSupplementaryUpdater) {

        this.appealPaymentConfirmationProvider = appealPaymentConfirmationProvider;
        this.feePayment = feePayment;
        this.postNotificationSender = postNotificationSender;
        this.scheduler = scheduler;
        this.dateProvider = dateProvider;
        this.ccdSupplementaryUpdater = ccdSupplementaryUpdater;
    }

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.PAYMENT_APPEAL;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        long caseId = callback.getCaseDetails().getId();
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        ccdSupplementaryUpdater.setHmctsServiceIdSupplementary(callback);

        // make a payment
        if (HandlerUtils.isAipJourney(asylumCase)) {
            return new PostSubmitCallbackResponse();
        }


        boolean isException = false;
        try {
            asylumCase = feePayment.aboutToSubmit(callback);

        } catch (Exception e) {
            isException = true;
            log.error("payment failed for caseId {}", caseId, e);
            asylumCase.write(PAYMENT_STATUS, TIMEOUT);
        }

        // rollback to payment pending state
        if (asylumCase.read(PAYMENT_STATUS, PaymentStatus.class).orElse(FAILED) != PAID) {

            AppealType appealType = asylumCase
                .read(APPEAL_TYPE, AppealType.class)
                .orElseThrow(() -> new IllegalStateException("no appealType chosen for caseId: " + caseId));

            Event rollbackEvent;

            if (asylumCase.read(PAYMENT_STATUS, PaymentStatus.class).orElse(FAILED) == TIMEOUT) {
                rollbackEvent = (AppealType.PA == appealType)
                    ? Event.ROLLBACK_PAYMENT_TIMEOUT : Event.ROLLBACK_PAYMENT_TIMEOUT_TO_PAYMENT_PENDING;
            } else {
                rollbackEvent = (Event.PAYMENT_APPEAL == callback.getEvent())
                    ? Event.ROLLBACK_PAYMENT : Event.MOVE_TO_PAYMENT_PENDING;
            }

            if (triggerRollbackToPaymentPendingState(caseId, rollbackEvent).isEmpty()) {
                // optionally we can use timed event status to send appropriate message to the Case Officer in case of rollback failure
            }
        }


        // send GovNotify notifications
        PostSubmitCallbackResponse postSubmitResponse = sendNotifications(callback, asylumCase);

        // apply custom success or error message based on the above actions
        return customiseConfirmationPage(callback.getCaseDetails().getId(), asylumCase, postSubmitResponse, isException);
    }

    private Optional<TimedEvent> triggerRollbackToPaymentPendingState(long caseId, Event event) {

        try {

            log.info("triggering rollback scenario for payAndSubmit event for caseId {}", caseId);
            return Optional.ofNullable(
                scheduler.schedule(
                    new TimedEvent(
                        "",
                        // trigger rollbackPayment only for paymentAppeal event - no state change rollback
                        event,
                        dateProvider.nowWithTime().atZone(ZoneId.systemDefault()),
                        "IA",
                        "Asylum",
                        caseId
                    )
                )
            );

        } catch (Exception e) {
            log.error("cannot trigger rollback scenario for payAndSubmit event for caseId {}", caseId, e);
        }

        return Optional.empty();
    }

    private PostSubmitCallbackResponse sendNotifications(Callback<AsylumCase> callback, AsylumCase asylumCase) {

        try {
            CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
            Callback<AsylumCase> callbackWithPaymentStatus = new Callback<>(
                new CaseDetails<>(
                    caseDetails.getId(),
                    caseDetails.getJurisdiction(),
                    caseDetails.getState(),
                    asylumCase,
                    caseDetails.getCreatedDate(),
                    caseDetails.getSecurityClassification(),
                    caseDetails.getSupplementaryData()
                ),
                callback.getCaseDetailsBefore(),
                callback.getEvent()
            );

            return postNotificationSender.send(callbackWithPaymentStatus);

        } catch (Exception e) {
            log.error(
                "cannot send notification for payAndSubmit event for caseId {}",
                callback.getCaseDetails().getId(),
                e
            );

        }

        return new PostSubmitCallbackResponse();
    }

    private PostSubmitCallbackResponse customiseConfirmationPage(
        long caseId,
        AsylumCase asylumCase,
        PostSubmitCallbackResponse postSubmitResponse,
        boolean isException) {

        final String paymentReferenceLabel = "\n\n#### Payment reference number\n";
        final String accountNumberLabel = "\n\n#### Payment by Account number\n";
        final String feeLabel = "\n\n#### Fee\n";

        if (appealPaymentConfirmationProvider.getPaymentStatus(asylumCase).equals(Optional.of(TIMEOUT))) {

            postSubmitResponse.setConfirmationHeader("");
            postSubmitResponse.setConfirmationBody(
                "![Payment failed confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/paymentFailed.png)\n\n"
                + "We have not been able to confirm if this payment has failed.\n\n"
                + "Email iapaymentexceptions@justice.gov.uk to check the status of this payment. Please include the HMCTS reference for the appeal.\n\n"
                + "The Tribunal will reply to confirm the payment status. If you still have to pay a fee, the reply will include instructions on how to make a payment."
            );
        } else if (appealPaymentConfirmationProvider.getPaymentStatus(asylumCase).equals(Optional.of(FAILED))) {

            String paymentErrorMessage = asylumCase.read(PAYMENT_ERROR_MESSAGE, String.class).orElse("");

            postSubmitResponse.setConfirmationHeader("");
            postSubmitResponse.setConfirmationBody(
                "![Payment failed confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/paymentFailed.png)\n"
                + "#### Do this next\n\n"
                + "Call 01633 652 125 (option 3) or email MiddleOffice.DDServices@liberata.com to try to resolve the payment issue. "
                + "If this is successful, follow the instructions on the overview tab to pay for the appeal using Payment by Account.\n\n"
                + "If you want to pay by card, you can [change the payment method](/case/IA/Asylum/"
                + caseId + "/trigger/editPaymentMethod) to card. The Tribunal will then contact you with payment instructions."
                + "\n#### Payment failed"
                + paymentReferenceLabel
                + appealPaymentConfirmationProvider.getPaymentReferenceNumber(asylumCase)
                + accountNumberLabel
                + appealPaymentConfirmationProvider.getPaymentAccountNumber(asylumCase)
                + feeLabel
                + "£" + appealPaymentConfirmationProvider.getFee(asylumCase)
                + "\n\n#### Reason for failed payment\n"
                + paymentErrorMessage
            );
        } else {

            YesOrNo submissionOutOfTime =
                requireNonNull(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)
                    .<RequiredFieldMissingException>orElseThrow(() -> new RequiredFieldMissingException("submission out of time is a required field")));

            if (submissionOutOfTime.equals(NO)) {
                postSubmitResponse.setConfirmationHeader("# Your appeal has been paid for and submitted");
                postSubmitResponse.setConfirmationBody(
                    "#### What happens next\n\n"
                    + "You will receive an email confirming that this appeal has been submitted successfully."
                    + "\n\n\n#### Payment successful"
                    + paymentReferenceLabel
                    + appealPaymentConfirmationProvider.getPaymentReferenceNumber(asylumCase)
                    + accountNumberLabel
                    + appealPaymentConfirmationProvider.getPaymentAccountNumber(asylumCase)
                    + feeLabel
                    + "£" + appealPaymentConfirmationProvider.getFee(asylumCase)
                );
            } else {
                postSubmitResponse.setConfirmationHeader("");
                postSubmitResponse.setConfirmationBody(
                    "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimePaidConfirmation.png)\n"
                    + "## What happens next\n\n"
                    + "A Tribunal Caseworker will review the reasons your appeal was out of time and you will be notified if it can proceed."
                    + "\n\n\n#### Payment successful"
                    + paymentReferenceLabel
                    + appealPaymentConfirmationProvider.getPaymentReferenceNumber(asylumCase)
                    + accountNumberLabel
                    + appealPaymentConfirmationProvider.getPaymentAccountNumber(asylumCase)
                    + feeLabel
                    + "£" + appealPaymentConfirmationProvider.getFee(asylumCase)
                );
            }
        }

        return postSubmitResponse;
    }
}
