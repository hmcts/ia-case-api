package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_DECISION;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@Component
public class AutomaticPaymentReminderRemissionLegalRepTrigger implements PreSubmitCallbackHandler<AsylumCase> {
    private final DateProvider dateProvider;
    private final Scheduler scheduler;
    @Value("${legalRepresentativeRemissionReminder.dueInMinutes}")
    int schedule7DaysInMinutes;

    public AutomaticPaymentReminderRemissionLegalRepTrigger(
        DateProvider dateProvider,
        Scheduler scheduler
    ) {
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

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.RECORD_REMISSION_DECISION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (isRejectedOrPartiallyApproved(asylumCase)) {
            ZonedDateTime scheduledDate = ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(schedule7DaysInMinutes);

            TimedEvent timedEvent = scheduler.schedule(
                new TimedEvent(
                    "",
                    Event.RECORD_REMISSION_REMINDER,
                    scheduledDate,
                    "IA",
                    "Asylum",
                    callback.getCaseDetails().getId()
                )
            );
            asylumCase.write(AsylumCaseFieldDefinition.AUTOMATIC_REMISSION_REMINDER_LEGAL_REP, timedEvent.getId());
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isRejectedOrPartiallyApproved(AsylumCase asylumCase) {
        return asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
            .map(decision -> decision == RemissionDecision.REJECTED || decision == RemissionDecision.PARTIALLY_APPROVED)
            .orElse(false);
    }
}