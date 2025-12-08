package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealReviewOutcome.DECISION_MAINTAINED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADA_HEARING_REQUIREMENTS_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_TRANSFERRED_OUT_OF_ADA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADD_APPEAL_RESPONSE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isNotificationTurnedOff;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.sourceOfAppealRehydratedAppeal;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealReviewOutcome;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
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
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();


        return timedEventServiceEnabled
                && !isNotificationTurnedOff(asylumCase)
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

        if (!isExAdaCaseWithHearingRequirementsSubmitted(asylumCase)
            && (callback.getEvent().equals(ADD_APPEAL_RESPONSE) || isEligibleForAutomaticDirection(callback))) {

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
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }


    private boolean isEligibleForAutomaticDirection(Callback<AsylumCase> callback) {
        final Optional<AppealReviewOutcome> reviewOutcome = callback.getCaseDetails().getCaseData()
            .read(AsylumCaseFieldDefinition.APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class);

        //Support the in-flight cases, where the homeoffice decision is not available
        if (!reviewOutcome.isPresent()) {
            if (HandlerUtils.isRepJourney(callback.getCaseDetails().getCaseData())) {
                return true;
            } else {
                throw new IllegalStateException("Appeal Review Outcome is mandatory");
            }
        }
        return callback.getEvent() == Event.REQUEST_RESPONSE_REVIEW
            && reviewOutcome.get() == DECISION_MAINTAINED;
    }

    private boolean isExAdaCaseWithHearingRequirementsSubmitted(AsylumCase asylumCase) {
        return asylumCase
                   .read(ADA_HEARING_REQUIREMENTS_SUBMITTED, YesOrNo.class)
                   .orElse(NO)
                   .equals(YES)
               && asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)
                   .orElse(NO)
                   .equals(YES);
    }
}
