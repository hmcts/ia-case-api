package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@Component
public class AutomaticDirectionRequestingHearingRequirementsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final boolean timedEventServiceEnabled;
    private final int reviewDueInDays;
    private final DateProvider dateProvider;
    private final Scheduler scheduler;
    private final FeatureToggler featureToggler;

    public AutomaticDirectionRequestingHearingRequirementsHandler(
        @Value("${featureFlag.timedEventServiceEnabled}") boolean timedEventServiceEnabled,
        @Value("${legalRepresentativeReview.dueInDays}") int reviewDueInDays,
        DateProvider dateProvider,
        Scheduler scheduler,
        FeatureToggler featureToggler
    ) {
        this.timedEventServiceEnabled = timedEventServiceEnabled;
        this.reviewDueInDays = reviewDueInDays;
        this.dateProvider = dateProvider;
        this.scheduler = scheduler;
        this.featureToggler = featureToggler;
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
               && Arrays.asList(
                    Event.REQUEST_RESPONSE_REVIEW,
                    Event.ADD_APPEAL_RESPONSE)
                   .contains(callback.getEvent());
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

        ZonedDateTime scheduledDate = ZonedDateTime.of(dateProvider.now().plusDays(reviewDueInDays + 1L), LocalTime.MIDNIGHT, ZoneId.systemDefault());

        if (featureToggler.getValue("timed-event-short-delay", false)) {
            int scheduleDelayInMinutes = 5;
            scheduledDate = ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(scheduleDelayInMinutes);
        }

        TimedEvent timedEvent = scheduler.schedule(
            new TimedEvent(
                "",
                Event.REQUEST_HEARING_REQUIREMENTS_FEATURE,
                // + 1 because you want to have full day on due date day for any changes and trigger event at night
                scheduledDate,
                "IA",
                "Asylum",
                callback.getCaseDetails().getId()
            )
        );

        asylumCase.write(AsylumCaseFieldDefinition.AUTOMATIC_DIRECTION_REQUESTING_HEARING_REQUIREMENTS, timedEvent.getId());

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
