package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StoredNotification;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NOTIFICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NOTIFICATIONS_SENT;

@Slf4j
@Component
public class SaveNotificationsToDataHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final NotificationClient notificationClient;
    private final Appender<StoredNotification> notificationAppender;

    private final FeatureToggler featureToggler;

    public SaveNotificationsToDataHandler(
        NotificationClient notificationClient,
        Appender<StoredNotification> notificationAppender,
        FeatureToggler featureToggler
    ) {
        this.notificationClient = notificationClient;
        this.notificationAppender = notificationAppender;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.SAVE_NOTIFICATIONS_TO_DATA
                && featureToggler.getValue("save-notifications-feature", false);
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

        Optional<List<IdValue<StoredNotification>>> maybeExistingNotifications =
            asylumCase.read(NOTIFICATIONS);

        Optional<List<IdValue<String>>> notificationsSent =
            asylumCase.read(NOTIFICATIONS_SENT);

        List<IdValue<StoredNotification>> allNotifications = maybeExistingNotifications.orElse(emptyList());
        List<String> notificationIds = getUnstoredNotificationIds(allNotifications, notificationsSent.orElse(emptyList()));
        if (!notificationIds.isEmpty()) {
            for (String notificationId : notificationIds) {
                try {
                    Notification notification = notificationClient.getNotificationById(notificationId);
                    StoredNotification storedNotification =
                        getStoredNotification(notificationId, notification);
                    allNotifications = notificationAppender.append(storedNotification, allNotifications);
                } catch (NotificationClientException exception) {
                    log.warn("Notification client error on case "
                        + callback.getCaseDetails().getId() + ": ", exception);
                }
            }
            allNotifications = sortNotificationsByDate(allNotifications);
            asylumCase.write(NOTIFICATIONS, allNotifications);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private static StoredNotification getStoredNotification(String notificationId, Notification notification) {
        String reference = notification.getReference().orElse(notificationId);
        String notificationBody = "<div>" + notification.getBody()
            .replace("\r\n", "<br>")
            .replace("’", "'")
            .replace("‘", "'")
            + "</div>";

        String method = notification.getNotificationType();
        String sentTo = switch (method) {
            case "email" -> notification.getEmailAddress().orElse("N/A");
            case "sms" -> notification.getPhoneNumber().orElse("N/A");
            default -> "N/A";
        };
        String status = notification.getStatus();
        List<String> failedStatus = List.of("permanent-failure", "temporary-failure", "technical-failure");
        status = failedStatus.contains(status) ? "Failed" : StringUtils.capitalize(status);
        ZonedDateTime zonedSentAt = notification.getSentAt().orElse(ZonedDateTime.now())
            .withZoneSameInstant(ZoneId.of("Europe/London"));
        String sentAt = zonedSentAt.toLocalDateTime().toString();
        String subject = notification.getSubject().orElse("N/A");
        return StoredNotification.builder()
            .notificationId(notificationId)
            .notificationDateSent(sentAt)
            .notificationSentTo(sentTo)
            .notificationBody(notificationBody)
            .notificationMethod(StringUtils.capitalize(method))
            .notificationStatus(status)
            .notificationReference(reference)
            .notificationSubject(subject)
            .build();
    }

    private List<String> getUnstoredNotificationIds(List<IdValue<StoredNotification>> storedNotifications,
                                                    List<IdValue<String>> sentNotificationIds) {
        List<String> storedNotificationIds = storedNotifications.stream()
            .map(idValue -> idValue.getValue().getNotificationId())
            .toList();
        return sentNotificationIds.stream()
            .filter(idValue -> filterNotificationsSentInTheLastSevenDays(idValue)
                        && !storedNotificationIds.contains(idValue.getValue()))
            .map(IdValue::getValue)
            .toList();
    }

    private boolean filterNotificationsSentInTheLastSevenDays(IdValue<String> idValue) {
        // Regular expression to match the timestamp at the end of the sentNotifications id
        String dateInEpochMillisPattern = "_(\\d{13})$";
        Pattern pattern = Pattern.compile(dateInEpochMillisPattern);;
        Matcher matcher = pattern.matcher(idValue.getId());

        if (matcher.find()) {
            // Calculate the timestamp in milliseconds for 7 days ago
            long sevenDaysAgoMillis = Instant.now().minusMillis(7L * 24 * 60 * 60 * 1000).toEpochMilli();

            long notificationDateInMillis = Long.parseLong(matcher.group(1));

            // Check if the timestamp is within the last 7 days
            return notificationDateInMillis >= sevenDaysAgoMillis;
        }

        return false;
    }

    private List<IdValue<StoredNotification>> sortNotificationsByDate(List<IdValue<StoredNotification>> allNotifications) {
        List<IdValue<StoredNotification>> mutableNotifications = new ArrayList<>(allNotifications);
        mutableNotifications.sort(Comparator.comparing(notification ->
            LocalDateTime.parse(notification.getValue().getNotificationDateSent()),
            Comparator.reverseOrder()
        ));
        return mutableNotifications;
    }

}
