package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@Component
public class AutomaticEndAppealForNonPaymentEaHuTrigger implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final Scheduler scheduler;

    @Value("${paymentEaHuNoRemission.dueInMinutes}")
    int schedule14DaysInMinutes;

    public AutomaticEndAppealForNonPaymentEaHuTrigger(
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

        Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
        Optional<AppealType> appealType = asylumCase.read(APPEAL_TYPE, AppealType.class);

        return  callback.getEvent() == Event.SUBMIT_APPEAL
                && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && (remissionType.isPresent() && remissionType.get() == RemissionType.NO_REMISSION)
                && (appealType.isPresent() && Set.of(EA, HU, EU, AG).contains(appealType.get()));
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback for auto end appeal for remission rejection");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        ZonedDateTime scheduledDate = ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(schedule14DaysInMinutes);

        TimedEvent timedEvent = scheduler.schedule(
            new TimedEvent(
                "",
                Event.END_APPEAL_AUTOMATICALLY,
                scheduledDate,
                "IA",
                "Asylum",
                callback.getCaseDetails().getId()
            )
        );
        asylumCase.write(AUTOMATIC_END_APPEAL_TIMED_EVENT_ID, timedEvent.getId());
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
