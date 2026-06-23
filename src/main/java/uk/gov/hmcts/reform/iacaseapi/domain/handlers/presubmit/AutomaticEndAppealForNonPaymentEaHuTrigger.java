package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption.WILL_PAY_FOR_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption;
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
    int schedule14DaysInMinutes;

    public AutomaticEndAppealForNonPaymentEaHuTrigger(
        DateProvider dateProvider,
        Scheduler scheduler,
        @Value("${paymentEaHuNoRemission.dueInMinutes}") int schedule14DaysInMinutes
    ) {
        this.dateProvider = dateProvider;
        this.scheduler = scheduler;
        this.schedule14DaysInMinutes = schedule14DaysInMinutes;
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


        boolean lrAppealWithNoRemission = remissionType.map(
                remission -> remission.equals(RemissionType.NO_REMISSION)).orElse(true);

        return  callback.getEvent() == Event.SUBMIT_APPEAL
                && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && !isAcceleratedDetainedAppeal(asylumCase)
                && (isAipJourney(asylumCase) ? !aipAppealHasRemission(asylumCase) : lrAppealWithNoRemission)
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

        if (isNotificationTurnedOff(asylumCase)) {
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

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

    private boolean aipAppealHasRemission(AsylumCase asylumCase) {
        Optional<RemissionOption> remissionOption = asylumCase.read(REMISSION_OPTION, RemissionOption.class);
        Optional<HelpWithFeesOption> helpWithFeesOption = asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class);
        return (remissionOption.isPresent() && remissionOption.get() != RemissionOption.NO_REMISSION)
                || (helpWithFeesOption.isPresent() && helpWithFeesOption.get() != WILL_PAY_FOR_APPEAL);
    }

}
