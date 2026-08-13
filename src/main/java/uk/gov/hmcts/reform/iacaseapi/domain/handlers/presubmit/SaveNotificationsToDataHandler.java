package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

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
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
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
    private final boolean saveNotificationToDataEnabled;
    private final FeatureToggler featureToggler;
    public static final List<String> validReferences = List.of(
        "STF_24WEEKS_REMOVAL_DECISION_LETTER_BUNDLE",
        "STF_24WEEKS_REMOVAL_DECISION_LETTER_LR_BUNDLE",
        "STF_24WEEKS_REMOVAL_REFUSED_DECISION_LETTER_BUNDLE",
        "STF_24WEEKS_REMOVAL_REFUSED_DECISION_LETTER_LR_BUNDLE"
    );

    public SaveNotificationsToDataHandler(
        NotificationClient notificationClient,
        @Value("${saveNotificationsData.enabled}") boolean saveNotificationToDataEnabled,
        FeatureToggler featureToggler
    ) {
        this.notificationClient = notificationClient;
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

        ArrayList<IdValue<StoredNotification>> allNotifications =
            new ArrayList<>(maybeExistingNotifications.orElse(emptyList()));
        List<String> notificationIds = getUnstoredNotificationIds(allNotifications,
            notificationsSent.orElse(emptyList()));
        if (!notificationIds.isEmpty()) {
            notificationIds.forEach(notificationId ->
                appendNotificationData(allNotifications, notificationId, callback));
            asylumCase.write(NOTIFICATIONS, sortAndReindexNotificationsByDate(allNotifications));
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void appendNotificationData(ArrayList<IdValue<StoredNotification>> allNotifications,
                                        String notificationId,
                                        Callback<AsylumCase> callback) {
        try {
            Notification notification = notificationClient.getNotificationById(notificationId);
            StoredNotification storedNotification =
                getStoredNotification(notificationId, notification, callback);
            allNotifications.addFirst(new IdValue<>("", storedNotification));
        } catch (NotificationClientException exception) {
            log.warn("Notification client error on case {}: ",
                callback.getCaseDetails().getId(), exception);
        }
    }


    public boolean isReferenceValidForLetterPdf(String notificationReference) {
        return validReferences.stream().anyMatch(notificationReference::contains);
    }

    public String getLetterEncodedPdfFile(String method,
                                          String notificationId,
                                          String notificationReference,
                                          Callback<AsylumCase> callback) {
        if (method.equalsIgnoreCase("letter") && isReferenceValidForLetterPdf(notificationReference)) {
            try {
                byte[] pdfFile = notificationClient.getPdfForLetter(notificationId);
                if (pdfFile != null && pdfFile.length > 0) {
                    return Base64.getEncoder().encodeToString(pdfFile);
                }
            } catch (NotificationClientException exception) {
                log.warn("Notification client getPdfForLetter failure on case {}: ",
                    callback.getCaseDetails().getId(), exception);
            }
        }
        return null;
    }

    private static void appendNonEmptyToString(String someString, StringBuilder stringBuilder, boolean withComma) {
        if (!someString.isBlank()) {
            stringBuilder.append(someString);
            if (withComma) {
                stringBuilder.append(", ");
            }
        }
    }

    private static String getAddressFromNotification(Notification notification) {
        StringBuilder addressBuilder = new StringBuilder();
        notification.getLine1().ifPresent(line1 -> appendNonEmptyToString(line1, addressBuilder, true));
        notification.getLine2().ifPresent(line2 -> appendNonEmptyToString(line2, addressBuilder, true));
        notification.getLine3().ifPresent(line3 -> appendNonEmptyToString(line3, addressBuilder, true));
        notification.getLine4().ifPresent(line4 -> appendNonEmptyToString(line4, addressBuilder, true));
        notification.getLine5().ifPresent(line5 -> appendNonEmptyToString(line5, addressBuilder, true));
        notification.getLine6().ifPresent(line6 -> appendNonEmptyToString(line6, addressBuilder, false));
        if (addressBuilder.toString().isBlank()) {
            return "N/A";
        }
        return addressBuilder.toString();
    }

    private StoredNotification getStoredNotification(String notificationId, Notification notification, Callback<AsylumCase> callback) {
        String reference = notification.getReference().orElse(notificationId);
        String notificationBody = "<div>" + notification.getBody()
            .replace("\r\n", "<br>")
            .replace("’", "'")
            .replace("‘", "'")
            .replace("“", "\"")
            .replace("”", "\"")
            .replace(" ", " ")
            .replace("&nbsp;", " ")
            + "</div>";

        String method = notification.getNotificationType();
        String sentTo = switch (method) {
            case "email" -> notification.getEmailAddress().orElse("N/A");
            case "sms" -> notification.getPhoneNumber().orElse("N/A");
            case "letter" -> getAddressFromNotification(notification);
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
            .notificationDocumentEncoded(getLetterEncodedPdfFile(method, notificationId, reference, callback))
            .notificationMethod(StringUtils.capitalize(method))
            .notificationStatus(status)
            .notificationReference(reference)
            .notificationSubject(subject)
            .build();
    }

    private boolean isNotificationAlreadyStored(List<IdValue<StoredNotification>> storedNotifications, String notificationId) {
        return storedNotifications.stream()
            .anyMatch(idValue -> idValue.getValue().getNotificationId().equals(notificationId));
    }

    private List<String> getUnstoredNotificationIds(List<IdValue<StoredNotification>> storedNotifications,
                                                    List<IdValue<String>> sentNotificationIds) {
        return sentNotificationIds.stream()
            .filter(this::filterNotificationsSentInTheLastSevenDays)
            .filter(idValue -> !isNotificationAlreadyStored(storedNotifications, idValue.getValue()))
            .map(IdValue::getValue)
            .toList();
    }

    private boolean filterNotificationsSentInTheLastSevenDays(IdValue<String> idValue) {
        // Regular expression to match the timestamp at the end of the sentNotifications id
        String dateInEpochMillisPattern = "_(\\d{13})$";
        Pattern pattern = Pattern.compile(dateInEpochMillisPattern);
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

    private List<IdValue<StoredNotification>> sortAndReindexNotificationsByDate(List<IdValue<StoredNotification>> allNotifications) {
        allNotifications.sort(Comparator.comparing(notification ->
                LocalDateTime.parse(notification.getValue().getNotificationDateSent()),
            Comparator.reverseOrder()
        ));
        return allNotifications.stream()
            .map(idValue ->
                new IdValue<>(String.valueOf(allNotifications.indexOf(idValue) + 1), idValue.getValue()))
            .toList();
    }

}
