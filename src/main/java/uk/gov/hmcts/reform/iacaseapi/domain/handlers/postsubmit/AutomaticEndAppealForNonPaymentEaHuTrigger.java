package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
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
               && (appealType.isPresent() && (appealType.get() == AppealType.EA || appealType.get() == AppealType.HU))
                && checkScenario(asylumCase);
    }

    private boolean checkScenario(AsylumCase asylumCase) {
        Optional<YesOrNo> isDetained = Optional.of(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class).orElse(YesOrNo.NO));
        Optional<YesOrNo> isAcceleratedDetainedAppeal = Optional.of(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class).orElse(YesOrNo.NO));
        Optional<AppealType> appealType = asylumCase.read(APPEAL_TYPE, AppealType.class);
        Optional<YesOrNo> isAgeAssessmentAppeal = Optional.of(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class).orElse(YesOrNo.NO));

        if (isDetained.equals(Optional.of(YesOrNo.YES))) {
            if (isAgeAssessmentAppeal.equals(Optional.of(YesOrNo.YES))) {
                return true;
            } else if (isAcceleratedDetainedAppeal.equals(Optional.of(YesOrNo.NO))
                    && checkAppealTypeIsHuEaEu(appealType)) {
                return true;
            } else {
                return false;
            }
        } else if (isDetained.equals(Optional.of(YesOrNo.NO))) {
            if (isAgeAssessmentAppeal.equals(Optional.of(YesOrNo.NO))) {
                return true;
            } else if (checkAppealTypeIsHuEaEu(appealType)) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    private boolean checkAppealTypeIsHuEaEu(Optional<AppealType> appealType) {
        String appealTypeName = appealType.get().name();

        return appealTypeName.equals(AppealType.HU.name())
                || appealTypeName.equals(AppealType.EA.name())
                || appealTypeName.equals(AppealType.EU.name());
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
