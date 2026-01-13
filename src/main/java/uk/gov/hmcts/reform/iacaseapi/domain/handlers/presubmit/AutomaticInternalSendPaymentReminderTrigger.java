package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@Component
public class AutomaticInternalSendPaymentReminderTrigger implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final Scheduler scheduler;

    @Value("${sendPaymentReminderNotification.dueInMinutesSinceSubmission}")
    int schedule7DaysInMinutes;

    public AutomaticInternalSendPaymentReminderTrigger(
        DateProvider dateProvider,
        Scheduler scheduler
    ) {
        this.dateProvider = dateProvider;
        this.scheduler = scheduler;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<PaymentStatus> paymentStatus = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class);

        return  callback.getEvent() == Event.SUBMIT_APPEAL
                && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && isInternalCase(asylumCase)
                && !isAppellantInDetention(asylumCase)
                && (paymentStatus.map(status -> status.equals(PaymentStatus.PAYMENT_PENDING)).orElse(false));
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (isNotificationTurnedOff(asylumCase)) {
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        ZonedDateTime scheduledDate = ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(schedule7DaysInMinutes);

        TimedEvent timedEvent = scheduler.schedule(
            new TimedEvent(
                "",
                Event.SEND_PAYMENT_REMINDER_NOTIFICATION,
                scheduledDate,
                "IA",
                "Asylum",
                callback.getCaseDetails().getId()
            )
        );
        asylumCase.write(AUTOMATIC_SEND_PAYMENT_REMINDER_TIMED_EVENT_ID, timedEvent.getId());
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
