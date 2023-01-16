package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@Component
public class AutomaticEndAppealForNonPaymentEaHuTrigger implements PostSubmitCallbackHandler<AsylumCase> {

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
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
        Optional<AppealType> appealType = asylumCase.read(APPEAL_TYPE, AppealType.class);

        return callback.getEvent() == Event.SUBMIT_APPEAL
                && (remissionType.isPresent() && remissionType.get() == RemissionType.NO_REMISSION)
                && (appealType.isPresent() && (appealType.get() == AppealType.EA || appealType.get() == AppealType.HU
                || checkCaseIsEligibleForAutomaticEndAppeal(asylumCase))
        );
    }

    private boolean checkCaseIsEligibleForAutomaticEndAppeal(AsylumCase asylumCase) {
        boolean isDetained = isAppellantInDetention(asylumCase);
        boolean isAcceleratedDetainedAppeal = isAcceleratedDetainedAppeal(asylumCase);
        boolean isAgeAssessmentAppeal = isAgeAssessmentAppeal(asylumCase);

        Optional<AppealType> appealType = asylumCase.read(APPEAL_TYPE, AppealType.class);

        if (isDetained) {
            if (isAgeAssessmentAppeal) {
                return true;
            } else if (!isAcceleratedDetainedAppeal && checkAppealTypeIsHuEaEu(appealType)) {
                return true;
            } else {
                return false;
            }
        } else {
            if (isAgeAssessmentAppeal) {
                return true;
            } else if (checkAppealTypeIsHuEaEu(appealType)) {
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean checkAppealTypeIsHuEaEu(Optional<AppealType> appealType) {
        return List.of(
                AppealType.HU.name(),
                AppealType.EA.name(),
                AppealType.EU.name()
        ).contains(appealType.get().name());
    }

    public PostSubmitCallbackResponse handle(
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback for auto end appeal for remission rejection");
        }

        ZonedDateTime scheduledDate = ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(schedule14DaysInMinutes);

        scheduler.schedule(
                new TimedEvent(
                        "",
                        Event.END_APPEAL_AUTOMATICALLY,
                        scheduledDate,
                        "IA",
                        "Asylum",
                        callback.getCaseDetails().getId()
                )
        );
        return new PostSubmitCallbackResponse();
    }
}
