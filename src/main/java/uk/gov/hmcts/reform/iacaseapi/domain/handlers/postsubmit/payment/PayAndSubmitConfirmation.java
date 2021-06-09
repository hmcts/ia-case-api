package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.payment;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.PAID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import java.time.ZoneId;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.AppealPaymentConfirmationProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PostNotificationSender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@Slf4j
@Component
public class PayAndSubmitConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final AppealPaymentConfirmationProvider appealPaymentConfirmationProvider;
    private final FeePayment<AsylumCase> feePayment;
    private final PostNotificationSender<AsylumCase> postNotificationSender;
    private final Scheduler scheduler;
    private final DateProvider dateProvider;

    public PayAndSubmitConfirmation(
        AppealPaymentConfirmationProvider appealPaymentConfirmationProvider,
        FeePayment<AsylumCase> feePayment,
        PostNotificationSender<AsylumCase> postNotificationSender,
        Scheduler scheduler,
        DateProvider dateProvider) {

        this.appealPaymentConfirmationProvider = appealPaymentConfirmationProvider;
        this.feePayment = feePayment;
        this.postNotificationSender = postNotificationSender;
        this.scheduler = scheduler;
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        long caseId = callback.getCaseDetails().getId();
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        // Scenario 1 - success by default
        // Scenario 2 - payment error - for example wrong PBA

        // make a payment
        try {
            asylumCase = feePayment.aboutToSubmit(callback);

            // Scenario 3 - payment timeout
            // throw new RuntimeException("error exception");
        } catch (Exception e) {

            log.error("payment failed for caseId {}", caseId, e);
            asylumCase.write(PAYMENT_STATUS, FAILED);
        }

        // rollback to payment pending state
        if (asylumCase.read(PAYMENT_STATUS, PaymentStatus.class).orElse(FAILED) != PAID) {

            if (triggerRollbackToPaymentPendingState(caseId).isEmpty()) {
                // optionally we can use timed event status to send appropriate message to the Case Officer in case of rollback failure
            }
        }

        // send GovNotify notifications
        PostSubmitCallbackResponse postSubmitResponse = sendNotifications(callback, asylumCase);
        // to Case Officer
        // to Legal Rep
        // to HO
        // to Appellant

        // apply custom success or error message based on the above actions
        return customiseConfirmationPage(callback.getCaseDetails().getId(), asylumCase, postSubmitResponse);

        // Scenario 6 - no CcdSubmitted callback at all
    }

    private Optional<TimedEvent> triggerRollbackToPaymentPendingState(long caseId) {

        try {

            log.info("triggering rollback scenario for payAndSubmit event for caseId {}", caseId);
            return Optional.ofNullable(
                scheduler.schedule(
                    new TimedEvent(
                        "",
                        Event.MOVE_TO_PAYMENT_PENDING,
                        dateProvider.nowWithTime().atZone(ZoneId.systemDefault()),
                        "IA",
                        "Asylum",
                        caseId
                    )
                )
            );

            // Scenario 4 and 5 - rollback failure
            // throw new RuntimeException("timed-event-service error");
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
                    caseDetails.getSecurityClassification()
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

    private PostSubmitCallbackResponse customiseConfirmationPage(long caseId, AsylumCase asylumCase, PostSubmitCallbackResponse postSubmitResponse) {

        final String paymentReferenceLabel = "\n\n#### Payment reference number\n";
        final String accountNumberLabel = "\n\n#### Payment by Account number\n";
        final String feeLabel = "\n\n#### Fee\n";

        if (appealPaymentConfirmationProvider.getPaymentStatus(asylumCase).equals(Optional.of(FAILED))) {

            String paymentErrorMessage = asylumCase.read(PAYMENT_ERROR_MESSAGE, String.class).orElse("");

            postSubmitResponse.setConfirmationHeader("");
            postSubmitResponse.setConfirmationBody(
                "![Payment failed confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/paymentFailed.png)\n"
                + "#### Do this next\n\n"
                + "Call 01633 652 125 (option 3) or email MiddleOffice.DDServices@liberata.com to try to resolve the payment issue.\n\n"
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
