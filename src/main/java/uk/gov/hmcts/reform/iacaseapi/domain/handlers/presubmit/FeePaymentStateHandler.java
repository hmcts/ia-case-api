package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@Component
public class FeePaymentStateHandler implements PreSubmitCallbackStateHandler<AsylumCase> {

    private final boolean isfeePaymentEnabled;
    private final boolean timedEventServiceEnabled;
    private final int paymentDueInDays;
    private final DateProvider dateProvider;
    private final Scheduler scheduler;
    private final FeatureToggler featureToggler;

    public FeePaymentStateHandler(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isfeePaymentEnabled,
        @Value("${featureFlag.timedEventServiceEnabled}") boolean timedEventServiceEnabled,
        @Value("${pendingPayment.dueInDays}") int paymentDueInDays,
        DateProvider dateProvider,
        Scheduler scheduler,
        FeatureToggler featureToggler
    ) {
        this.isfeePaymentEnabled = isfeePaymentEnabled;
        this.timedEventServiceEnabled = timedEventServiceEnabled;
        this.paymentDueInDays = paymentDueInDays;
        this.dateProvider = dateProvider;
        this.scheduler = scheduler;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return timedEventServiceEnabled
               && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.SUBMIT_APPEAL
               && isfeePaymentEnabled;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback,
        PreSubmitCallbackResponse<AsylumCase> callbackResponse
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final State currentState =
            callback
                .getCaseDetails()
                .getState();

        final boolean isPaymentPendingAppealType = asylumCase
            .read(APPEAL_TYPE, AppealType.class)
            .map(type -> type == HU || type == EA).orElse(false);

        final String paymentStatus = asylumCase
            .read(PAYMENT_STATUS, String.class).orElse("");

        if (isPaymentPendingAppealType && paymentStatus.equals("Payment due")) {
            triggerTimedEvent(asylumCase, callback);
            return new PreSubmitCallbackResponse<>(asylumCase, State.PAYMENT_PENDING);

        } else {
            return new PreSubmitCallbackResponse<>(asylumCase, currentState);
        }
    }

    public void triggerTimedEvent(AsylumCase asylumCase, Callback<AsylumCase> callback) {

        ZonedDateTime scheduledDate = ZonedDateTime.of(dateProvider.now().plusDays(paymentDueInDays + 1L), LocalTime.MIDNIGHT, ZoneId.systemDefault());

        if (featureToggler.getValue("timed-event-short-delay", false)) {
            int scheduleDelayInMinutes = 5;
            scheduledDate = ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(scheduleDelayInMinutes);
        }

        TimedEvent timedEvent = scheduler.schedule(
            new TimedEvent(
                "",
                Event.END_APPEAL,
                // + 1 because you want to have full day on due date day for any changes and trigger event at night
                scheduledDate,
                "IA",
                "Asylum",
                callback.getCaseDetails().getId()
            )
        );

        asylumCase.write(AsylumCaseFieldDefinition.AUTOMATIC_END_APPEAL, timedEvent.getId());
    }
}
