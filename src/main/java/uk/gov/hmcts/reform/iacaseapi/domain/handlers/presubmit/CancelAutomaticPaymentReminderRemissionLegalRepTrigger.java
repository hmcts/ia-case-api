package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AUTOMATIC_REMISSION_REMINDER_LEGAL_REP;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

public class CancelAutomaticPaymentReminderRemissionLegalRepTrigger implements PreSubmitCallbackHandler<AsylumCase> {

    private final boolean timedEventServiceEnabled;
    private final DateProvider dateProvider;
    private final Scheduler scheduler;

    public CancelAutomaticPaymentReminderRemissionLegalRepTrigger(
        @Value("${featureFlag.timedEventServiceEnabled}") boolean timedEventServiceEnabled,
        DateProvider dateProvider,
        Scheduler scheduler
    ) {
        this.timedEventServiceEnabled = timedEventServiceEnabled;
        this.dateProvider = dateProvider;
        this.scheduler = scheduler;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATE;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return timedEventServiceEnabled
               && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.RECORD_REMISSION_DECISION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<String> timeEventId = asylumCase.read(AUTOMATIC_REMISSION_REMINDER_LEGAL_REP);

        if (timeEventId.isPresent()) {
            int scheduleDelayInMinutes = 52560000;
            ZonedDateTime scheduledDate = ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(scheduleDelayInMinutes);

            scheduler.schedule(
                new TimedEvent(
                    timeEventId.get(),
                    Event.RECORD_REMISSION_REMINDER,
                    scheduledDate,
                    "IA",
                    "Asylum",
                    callback.getCaseDetails().getId()
                )
            );

        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
