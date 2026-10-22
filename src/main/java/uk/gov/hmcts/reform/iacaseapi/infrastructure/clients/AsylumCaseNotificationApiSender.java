package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static java.time.LocalDate.parse;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SAVE_NOTIFICATIONS_TO_DATA_DATE;

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
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.SaveNotificationsDataConfiguration;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

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
    private final SaveNotificationsDataConfiguration saveNotificationsConfig;

    public AsylumCaseNotificationApiSender(
        AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator,
        @Value("${notificationsApi.endpoint}") String notificationsApiEndpoint,
        @Value("${notificationsApi.aboutToSubmitPath}") String aboutToSubmitPath,
        @Value("${featureFlag.timedEventServiceEnabled}") boolean timedEventServiceEnabled,
        DateProvider dateProvider,
        Scheduler scheduler,
        FeatureToggler featureToggler,
        SaveNotificationsDataConfiguration saveNotificationsConfig
    ) {
        this.asylumCaseCallbackApiDelegator = asylumCaseCallbackApiDelegator;
        this.notificationsApiEndpoint = notificationsApiEndpoint;
        this.aboutToSubmitPath = aboutToSubmitPath;
        this.timedEventServiceEnabled = timedEventServiceEnabled;
        this.dateProvider = dateProvider;
        this.scheduler = scheduler;
        this.featureToggler = featureToggler;
        this.saveNotificationsConfig = saveNotificationsConfig;
    }

    public AsylumCase send(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        boolean isEnabled = saveNotificationsConfig.isEnabled();
        boolean featureTogglerValue = featureToggler.getValue("save-notifications-feature", false);

        if (featureTogglerValue && isEnabled) {
            AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
            Optional<String> saveNotificationToDataDateOpt = asylumCase.read(SAVE_NOTIFICATIONS_TO_DATA_DATE, String.class);
            if (saveNotificationToDataDateOpt.isEmpty()
                    || parse(saveNotificationToDataDateOpt.get()).isBefore(LocalDate.now())) {
                scheduleSaveNotificationToData(callback);
                String saveNotificationsToDataDate = LocalDate.now().toString();
                log.info("Writing saveNotificationsToDataDate to caseData: {}", saveNotificationsToDataDate);
                asylumCase.write(SAVE_NOTIFICATIONS_TO_DATA_DATE, saveNotificationsToDataDate);
            } else {
                log.info("saveNotificationsToDataDate field already present: {}", saveNotificationToDataDateOpt.get());
            }
        } else {
            log.info("Skipping saveNotificationsToData event schedule. saveNotificationsConfig: {}, "
                    + "save-notifications-feature: {}", isEnabled, featureTogglerValue);
        }

        return asylumCaseCallbackApiDelegator.delegate(
            callback,
            notificationsApiEndpoint + aboutToSubmitPath
        );
    }

    private void scheduleSaveNotificationToData(Callback<AsylumCase> callback) {
        if (timedEventServiceEnabled) {
            try {
                ZonedDateTime currentTime = ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault());
                ZonedDateTime scheduledTime = saveNotificationsConfig.calculateScheduledTime(currentTime);

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
                log.error("Scheduling SAVE_NOTIFICATIONS_TO_DATA event failed for case reference {}, event name: {}",
                        callback.getCaseDetails().getId(), callback.getEvent().toString(), e);
            }
        }
    }
}
