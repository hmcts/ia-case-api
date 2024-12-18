package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Service
public class AsylumCaseNotificationApiSender implements NotificationSender<AsylumCase> {

    private final AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;
    private final String notificationsApiEndpoint;
    private final String aboutToSubmitPath;
    private final boolean timedEventServiceEnabled;
    private final DateProvider dateProvider;
    private final Scheduler scheduler;
    private final FeatureToggler featureToggler;

    public AsylumCaseNotificationApiSender(
        AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator,
        @Value("${notificationsApi.endpoint}") String notificationsApiEndpoint,
        @Value("${notificationsApi.aboutToSubmitPath}") String aboutToSubmitPath,
        @Value("${featureFlag.timedEventServiceEnabled}") boolean timedEventServiceEnabled,
        DateProvider dateProvider,
        Scheduler scheduler,
        FeatureToggler featureToggler
    ) {
        this.asylumCaseCallbackApiDelegator = asylumCaseCallbackApiDelegator;
        this.notificationsApiEndpoint = notificationsApiEndpoint;
        this.aboutToSubmitPath = aboutToSubmitPath;
        this.timedEventServiceEnabled = timedEventServiceEnabled;
        this.dateProvider = dateProvider;
        this.scheduler = scheduler;
        this.featureToggler = featureToggler;
    }

    public AsylumCase send(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        if (featureToggler.getValue("save-notifications-feature", false)) {
            scheduleSaveNotificationToData(callback);
        }

        return asylumCaseCallbackApiDelegator.delegate(
            callback,
            notificationsApiEndpoint + aboutToSubmitPath
        );
    }

    private void scheduleSaveNotificationToData(Callback<AsylumCase> callback) {
        if (timedEventServiceEnabled) {
            ZonedDateTime scheduledTime = ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault())
                    .plusSeconds(15);
            try {
                scheduler.schedule(
                        new TimedEvent(
                                "",
                                Event.SAVE_NOTIFICATIONS_TO_DATA,
                                scheduledTime,
                                "IA",
                                "Asylum",
                                callback.getCaseDetails().getId()
                        )
                );
            } catch (AsylumCaseServiceResponseException e) {
                log.error("Scheduling SAVE_NOTIFICATIONS_TO_DATA event failed: ", e);
            }
        }
    }
}
