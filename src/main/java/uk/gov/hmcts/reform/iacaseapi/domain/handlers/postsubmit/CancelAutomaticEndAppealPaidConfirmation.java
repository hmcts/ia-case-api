package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AUTOMATIC_END_APPEAL_TIMED_EVENT_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@Component
@Slf4j
public class CancelAutomaticEndAppealPaidConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final boolean timedEventServiceEnabled;
    private final Scheduler scheduler;


    public CancelAutomaticEndAppealPaidConfirmation(
            @Value("${featureFlag.timedEventServiceEnabled}") boolean timedEventServiceEnabled,
            Scheduler scheduler
    ) {
        this.timedEventServiceEnabled = timedEventServiceEnabled;
        this.scheduler = scheduler;
    }

    public boolean canHandle(
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PaymentStatus paymentStatus = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)
                .orElse(PaymentStatus.PAYMENT_PENDING);
        Optional<String> timedEventId = asylumCase.read(AUTOMATIC_END_APPEAL_TIMED_EVENT_ID);

        Event qualifyingEvent = HandlerUtils.isAipJourney(asylumCase)
            ? Event.PAYMENT_APPEAL
            : Event.UPDATE_PAYMENT_STATUS;

        return  timedEventServiceEnabled
                && callback.getEvent() == qualifyingEvent
                && paymentStatus == PaymentStatus.PAID
                && timedEventId.isPresent();

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

        Optional<String> timeEventId = asylumCase.read(AUTOMATIC_END_APPEAL_TIMED_EVENT_ID);

        if (timeEventId.isPresent()) {
            boolean result = scheduler.deleteSchedule(timeEventId.get());
            if (!result) {
                log.warn("Could not delete the outdated payments schedule for case id {}. " +
                        "If the scheduled task is still present, this will result in a fail-safe triggering in the " +
                        "EndAppealHandler class, which will produce an exception. " +
                        "Do not touch the fail-safe! Look into the reason of this failure instead.",
                    callback.getCaseDetails().getId());
            }
        }

        return new PostSubmitCallbackResponse();
    }

}
