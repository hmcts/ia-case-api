package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NOTIFICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NOTIFICATIONS_SENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.SaveNotificationsToDataHandler.NotificationType.*;

@Slf4j
@Component
public class SaveNotificationsToDataHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final List<String> FAILED_STATUSES = List.of("permanent-failure", "temporary-failure", "technical-failure");
    private static final ZoneId LONDON_ZONE = ZoneId.of("Europe/London");
    private static final Pattern EPOCH_MILLIS_PATTERN = Pattern.compile("_(\\d{13})$");

    private final NotificationClient notificationClient;
    private final Appender<StoredNotification> notificationAppender;
    private final boolean saveNotificationToDataEnabled;
    private final FeatureToggler featureToggler;

    public SaveNotificationsToDataHandler(
        NotificationClient notificationClient,
        Appender<StoredNotification> notificationAppender,
        @Value("${saveNotificationsData.enabled}") boolean saveNotificationToDataEnabled,
        FeatureToggler featureToggler
    ) {
        this.notificationClient = notificationClient;
        this.notificationAppender = notificationAppender;
        this.featureToggler = featureToggler;
        this.saveNotificationToDataEnabled = saveNotificationToDataEnabled;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.SAVE_NOTIFICATIONS_TO_DATA
                && featureToggler.getValue("save-notifications-feature", false)
                && saveNotificationToDataEnabled;
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
                    StoredNotification storedNotification = switch (typeOf(notification)) {
                        case STANDARD_LETTER -> getStoredNotificationForLetter(notificationId, notification);
                        case PRE_COMPILED_LETTER -> getStoredNotificationForPrecompiledLetter(notificationId, notification);
                        case EMAIL -> getStoredNotification(notificationId, notification);
                    };
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

    private StoredNotification getStoredNotificationForPrecompiledLetter(String notificationId, Notification notification) throws NotificationClientException {
        byte[] pdfForLetter = notificationClient.getPdfForLetter(notificationId);
        String base64PdfLetter = Base64.getEncoder().encodeToString(pdfForLetter);

        return buildStoredNotification(notificationId, notification)
            .notificationSentTo("See PDF attachment for recipient details")
            .notificationBody(base64PdfLetter)
            .notificationSubject("See PDF attachment for details")
            .build();
    }

    private static StoredNotification getStoredNotificationForLetter(String notificationId, Notification notification) {
        String addressHtml = buildAddressHtml(notification);
        String notificationBody = "<div>" + addressHtml + formatBody(notification.getBody()) + "</div>";

        return buildStoredNotification(notificationId, notification)
            .notificationSentTo("See PDF attachment for recipient details")
            .notificationBody(notificationBody)
            .notificationSubject(notification.getSubject().orElse("N/A"))
            .build();
    }

    private static StoredNotification getStoredNotification(String notificationId, Notification notification) {
        String notificationBody = "<div>" + formatBody(notification.getBody()) + "</div>";
        String method = notification.getNotificationType();
        String sentTo = switch (method) {
            case "email" -> notification.getEmailAddress().orElse("N/A");
            case "sms" -> notification.getPhoneNumber().orElse("N/A");
            default -> "N/A";
        };

        return buildStoredNotification(notificationId, notification)
            .notificationSentTo(sentTo)
            .notificationBody(notificationBody)
            .notificationSubject(notification.getSubject().orElse("N/A"))
            .build();
    }

    private static StoredNotification.StoredNotificationBuilder buildStoredNotification(String notificationId, Notification notification) {
        String reference = notification.getReference().orElse(notificationId);
        String status = resolveStatus(notification.getStatus());
        String sentAt = notification.getSentAt().orElse(ZonedDateTime.now())
            .withZoneSameInstant(LONDON_ZONE)
            .toLocalDateTime().toString();

        return StoredNotification.builder()
            .notificationId(notificationId)
            .notificationDateSent(sentAt)
            .notificationMethod(StringUtils.capitalize(notification.getNotificationType()))
            .notificationStatus(status)
            .notificationReference(reference);
    }

    private static String resolveStatus(String status) {
        return FAILED_STATUSES.contains(status) ? "Failed" : StringUtils.capitalize(status);
    }

    private static String formatBody(String body) {
        return body.replace("\r\n", "<br>")
            .replace("’", "'")
            .replace("’", "'");
    }

    private static String buildAddressHtml(Notification notification) {
        String address = Stream.of(
                notification.getLine1(),
                notification.getLine2(),
                notification.getLine3(),
                notification.getLine4(),
                notification.getLine5(),
                notification.getLine6())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(line -> !line.isBlank())
            .collect(Collectors.joining("<br>"));
        return address.isEmpty() ? "" : address + "<br><br>";
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
        Matcher matcher = EPOCH_MILLIS_PATTERN.matcher(idValue.getId());

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

    enum NotificationType {
        STANDARD_LETTER,
        PRE_COMPILED_LETTER,
        EMAIL;

        public static NotificationType typeOf(Notification notification) {
            String addressLine1 = notification.getLine1().orElse("");
            if (notification.getNotificationType().equals("email")) {
                return EMAIL;
            }
            if (notification.getNotificationType().equals("letter") && addressLine1.equals("Provided as PDF")) {
                return PRE_COMPILED_LETTER;
            }
            if (notification.getNotificationType().equals("letter")) {
                return STANDARD_LETTER;
            }

            throw new IllegalStateException("Unknown notification type");
        }
    }

}
