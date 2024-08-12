package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
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
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NOTIFICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NOTIFICATIONS_SENT;

@Slf4j
@Component
public class SaveNotificationsToDataHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final NotificationClient notificationClient;
    private final Appender<StoredNotification> notificationAppender;


    public SaveNotificationsToDataHandler(
        NotificationClient notificationClient,
        Appender<StoredNotification> notificationAppender
    ) {
        this.notificationClient = notificationClient;
        this.notificationAppender = notificationAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.SAVE_NOTIFICATIONS_TO_DATA;
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
        for (String notificationId: notificationIds) {
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
        asylumCase.write(NOTIFICATIONS, allNotifications);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private static StoredNotification getStoredNotification(String notificationId, Notification notification) {
        String reference = notification.getReference().orElse(notificationId);
        String sentTo = notification.getEmailAddress()
            .orElse(notification.getPhoneNumber()
                .orElse("N/A"));
        String notificationBody = "<div>" + notification.getBody().split("First-tier")[0]
            .split("---")[0].replace("\r\n", "<br>") + "</div>";

        String method = notification.getNotificationType();
        String status = notification.getStatus();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String sentAt = notification.getSentAt().orElse(ZonedDateTime.now()).format(formatter);
        return new StoredNotification(notificationId, sentAt, sentTo, notificationBody, method, status, reference);
    }

    private List<String> getUnstoredNotificationIds(List<IdValue<StoredNotification>> storedNotifications,
                                                    List<IdValue<String>> sentNotificationIds) {
        List<String> storedNotificationIds = storedNotifications.stream()
            .map(idValue -> idValue.getValue().getNotificationId())
            .toList();
        return sentNotificationIds.stream()
            .filter(idValue -> !storedNotificationIds.contains(idValue.getValue()))
            .map(IdValue::getValue)
            .toList();
    }
}
