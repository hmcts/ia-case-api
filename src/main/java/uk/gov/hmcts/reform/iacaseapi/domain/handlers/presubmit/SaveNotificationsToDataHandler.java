package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StoredNotification;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NOTIFICATIONS;

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

        String notificationId = asylumCase.read(AsylumCaseFieldDefinition.TEST_NOTIFICATION_ID, String.class)
            .orElse("");

        if (!notificationId.isEmpty()) {
            try {
                Notification notification = notificationClient.getNotificationById(notificationId);
                log.info("Here is the notification stuff");
                String sentAt = notification.getSentAt().orElse(ZonedDateTime.now()).toString();
                String sentTo = notification.getEmailAddress()
                    .orElse(notification.getPhoneNumber()
                        .orElse("N/A"));
                // String notificationBody = "<div>" + notification.getBody().split("First-tier")[0]
                //     .split("---")[0].replaceAll("\r\n", "<br>") + "</div>";
                Document document = new Document("", "",
                    notification.getReference().orElse("notification_file_name") + ".pdf");
                String method = notification.getNotificationType();
                String status = notification.getStatus();
                StoredNotification storedNotification =
                    new StoredNotification(sentAt, sentTo, document, method, status);
                Optional<List<IdValue<StoredNotification>>> maybeExistingNotifications =
                    asylumCase.read(NOTIFICATIONS);
                List<IdValue<StoredNotification>> allNotifications =
                    notificationAppender.append(storedNotification, maybeExistingNotifications
                        .orElse(emptyList()));
                asylumCase.write(NOTIFICATIONS, allNotifications);
                log.info("notification.getBody(): {}", notification.getBody());
                log.info("notification.getId(): {}", notification.getId());
                log.info("notification.getNotificationType(): {}", notification.getNotificationType());
                log.info("notification.getCompletedAt(): {}", notification.getCompletedAt());
                log.info("notification.getCreatedByName(): {}", notification.getCreatedByName());
                log.info("notification.getEmailAddress(): {}", notification.getEmailAddress());
                log.info("notification.getEstimatedDelivery(): {}", notification.getEstimatedDelivery());
                log.info("notification.getLine1(): {}", notification.getLine1());
                log.info("notification.getLine2(): {}", notification.getLine2());
                log.info("notification.getLine3(): {}", notification.getLine3());
                log.info("notification.getLine4(): {}", notification.getLine4());
                log.info("notification.getLine5(): {}", notification.getLine5());
                log.info("notification.getLine6(): {}", notification.getLine6());
                log.info("notification.getPhoneNumber(): {}", notification.getPhoneNumber());
                log.info("notification.getPostage(): {}", notification.getPostage());
                log.info("notification.getPostcode(): {}", notification.getPostcode());
                log.info("notification.getReference(): {}", notification.getReference());
                log.info("notification.getSentAt(): {}", notification.getSentAt());
                log.info("notification.getStatus(): {}", notification.getStatus());
                log.info("notification.getSubject(): {}", notification.getSubject());
            } catch (NotificationClientException exception) {
                log.warn("Notification client error: ", exception);
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
