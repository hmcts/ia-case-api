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

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
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
    private final boolean saveNotificationToDataEnabled;
    private final int saveNotificationScheduleAtHour;
    private final int saveNotificationScheduleMaxMinutes;
    private final DateProvider dateProvider;
    private final Scheduler scheduler;
    private final FeatureToggler featureToggler;
    SecureRandom random = new SecureRandom();

    public AsylumCaseNotificationApiSender(
        AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator,
        @Value("${notificationsApi.endpoint}") String notificationsApiEndpoint,
        @Value("${notificationsApi.aboutToSubmitPath}") String aboutToSubmitPath,
        @Value("${featureFlag.timedEventServiceEnabled}") boolean timedEventServiceEnabled,
        @Value("${saveNotificationsData.enabled}") boolean saveNotificationToDataEnabled,
        @Value("${saveNotificationsData.scheduleAtHour}") int saveNotificationScheduleAtHour,
        @Value("${saveNotificationsData.scheduleMaxMinutes}") int saveNotificationScheduleMaxMinutes,
        DateProvider dateProvider,
        Scheduler scheduler,
        FeatureToggler featureToggler
    ) {
        this.asylumCaseCallbackApiDelegator = asylumCaseCallbackApiDelegator;
        this.notificationsApiEndpoint = notificationsApiEndpoint;
        this.aboutToSubmitPath = aboutToSubmitPath;
        this.timedEventServiceEnabled = timedEventServiceEnabled;
        this.saveNotificationToDataEnabled = saveNotificationToDataEnabled;
        this.saveNotificationScheduleAtHour = saveNotificationScheduleAtHour;
        this.saveNotificationScheduleMaxMinutes = saveNotificationScheduleMaxMinutes;
        this.dateProvider = dateProvider;
        this.scheduler = scheduler;
        this.featureToggler = featureToggler;
    }

    public AsylumCase send(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        log.info("saveNotificationToDataEnabled env var value: {}", saveNotificationToDataEnabled);
        boolean featureTogglerValue = featureToggler.getValue("save-notifications-feature", false);
        log.info("save-notifications-feature LD flag value: {}", featureTogglerValue);

        if (featureTogglerValue && saveNotificationToDataEnabled) {
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
            log.info("Skipping saveNotificationsToData event schedule");
        }

        return asylumCaseCallbackApiDelegator.delegate(
            callback,
            notificationsApiEndpoint + aboutToSubmitPath
        );
    }

    private void scheduleSaveNotificationToData(Callback<AsylumCase> callback) {
        if (timedEventServiceEnabled) {

            try {
                scheduler.schedule(
                        new TimedEvent(
                                "",
                                Event.SAVE_NOTIFICATIONS_TO_DATA,
                                determineScheduleTime(),
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

    private ZonedDateTime determineScheduleTime() {
        ZonedDateTime now = ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault());
        // Define saveNotificationScheduleAtHour eg:11:00 PM as the base time
        LocalTime baseTime = LocalTime.of(saveNotificationScheduleAtHour, 0);

        // Randomize minutes and seconds between 0-saveNotificationScheduleMaxMinutes minutes and 0-59 seconds
        int randomMinutes = random.nextInt(0, saveNotificationScheduleMaxMinutes);
        int randomSeconds = random.nextInt(0, 60);

        // If notification sent time is before saveNotificationScheduleAtHour
        if (now.toLocalTime().isBefore(baseTime)) {
            return now.toLocalDate().atTime(baseTime).plusMinutes(randomMinutes).plusSeconds(randomSeconds)
                    .atZone(now.getZone());
        } else {
            // If notification sent time is after saveNotificationScheduleAtHour eg: 11 PM, schedule for the next day
            return now.toLocalDate().plusDays(1).atTime(baseTime).plusMinutes(randomMinutes).plusSeconds(randomSeconds)
                    .atZone(now.getZone());
        }
    }

}
