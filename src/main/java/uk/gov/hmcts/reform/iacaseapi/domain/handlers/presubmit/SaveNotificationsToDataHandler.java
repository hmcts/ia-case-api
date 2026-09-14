package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.SaveNotificationsDataConfiguration;
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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NOTIFICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NOTIFICATIONS_SENT;

@Slf4j
@Component
public class SaveNotificationsToDataHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String NOT_AVAILABLE = "N/A";
    private static final ZoneId UK_TIMEZONE = ZoneId.of("Europe/London");
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("_(\\d{13})$");
    private static final List<String> FAILED_STATUSES = List.of(
        "permanent-failure", "temporary-failure", "technical-failure"
    );
    public static final List<String> VALID_REFERENCES = List.of(
        "STF_24WEEKS_REMOVAL_DECISION_LETTER_BUNDLE",
        "STF_24WEEKS_REMOVAL_DECISION_LETTER_LR_BUNDLE",
        "STF_24WEEKS_REMOVAL_REFUSED_DECISION_LETTER_BUNDLE",
        "STF_24WEEKS_REMOVAL_REFUSED_DECISION_LETTER_LR_BUNDLE"
    );
    private static final List<String> SUCCESSFUL_STATUSES = List.of(
        "Sent", "Delivered", "Returned-letter", "Received", "Failed"
    );

    private final NotificationClient notificationClient;
    private final SaveNotificationsDataConfiguration configuration;
    private final FeatureToggler featureToggler;
    private final Appender<StoredNotification> notificationAppender;

    public SaveNotificationsToDataHandler(
        NotificationClient notificationClient,
        SaveNotificationsDataConfiguration configuration,
        FeatureToggler featureToggler,
        Appender<StoredNotification> notificationAppender
    ) {
        this.notificationClient = notificationClient;
        this.configuration = configuration;
        this.featureToggler = featureToggler;
        this.notificationAppender = notificationAppender;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.SAVE_NOTIFICATIONS_TO_DATA
            && featureToggler.getValue("save-notifications-feature", false)
            && configuration.isEnabled();
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<List<IdValue<StoredNotification>>> maybeExistingNotifications =
            asylumCase.read(NOTIFICATIONS);
        Optional<List<IdValue<String>>> maybeSentNotificationIds =
            asylumCase.read(NOTIFICATIONS_SENT);

        List<IdValue<String>> sentNotificationIds = maybeSentNotificationIds.orElse(emptyList());
        List<IdValue<StoredNotification>> existingNotifications = maybeExistingNotifications.orElse(emptyList());

        // Update existing notifications that haven't reached a successful status
        List<String> notificationIdsToUpdate = getNotificationIdsToUpdate(existingNotifications, sentNotificationIds);
        List<IdValue<StoredNotification>> allNotifications = new ArrayList<>(existingNotifications);
        for (String id : notificationIdsToUpdate) {
            updateNotificationData(allNotifications, id, callback);
        }

        // Append new notifications
        Set<String> unstoredIds = findUnstoredNotificationIds(allNotifications, sentNotificationIds);
        for (String id : unstoredIds) {
            Optional<StoredNotification> notification = fetchNotification(id, callback);
            if (notification.isPresent()) {
                allNotifications = notificationAppender.append(notification.get(), allNotifications);
            }
        }

        List<IdValue<StoredNotification>> deduplicatedNotifications = removeDuplicateNotifications(allNotifications);
        if (deduplicatedNotifications.size() < allNotifications.size()) {
            log.info("Removed {} duplicate notifications from case {}",
                allNotifications.size() - deduplicatedNotifications.size(),
                callback.getCaseDetails().getId());
        }

        asylumCase.write(NOTIFICATIONS, sortAndReindexNotificationsByDate(deduplicatedNotifications));

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private Optional<StoredNotification> fetchNotification(String notificationId, Callback<AsylumCase> callback) {
        try {
            Notification notification = notificationClient.getNotificationById(notificationId);
            return Optional.of(buildStoredNotification(notificationId, notification, callback));
        } catch (NotificationClientException e) {
            log.warn("Notification client error on case {}: ", callback.getCaseDetails().getId(), e);
            return Optional.empty();
        }
    }

    private void updateNotificationData(
        List<IdValue<StoredNotification>> allNotifications,
        String notificationId,
        Callback<AsylumCase> callback
    ) {
        try {
            Notification notification = notificationClient.getNotificationById(notificationId);
            StoredNotification storedNotification = buildStoredNotification(notificationId, notification, callback);
            allNotifications.stream()
                .filter(n -> n.getValue().getNotificationId().equals(notificationId))
                .findFirst()
                .ifPresent(existing -> {
                    allNotifications.remove(existing);
                    allNotifications.add(new IdValue<>("", storedNotification));
                });
        } catch (NotificationClientException e) {
            log.warn("Notification client error on case {}: ", callback.getCaseDetails().getId(), e);
        }
    }

    private StoredNotification buildStoredNotification(
        String notificationId,
        Notification notification,
        Callback<AsylumCase> callback
    ) {
        String reference = notification.getReference().orElse(notificationId);
        String method = notification.getNotificationType();

        return StoredNotification.builder()
            .notificationId(notificationId)
            .notificationDateSent(formatSentDate(notification))
            .notificationSentTo(extractRecipient(notification, method))
            .notificationBody(sanitizeBody(notification.getBody()))
            .notificationDocumentEncoded(getLetterEncodedPdfFile(method, notificationId, reference, callback))
            .notificationMethod(StringUtils.capitalize(method))
            .notificationStatus(normalizeStatus(notification.getStatus()))
            .notificationReference(reference)
            .notificationSubject(notification.getSubject().orElse(NOT_AVAILABLE))
            .build();
    }

    private String formatSentDate(Notification notification) {
        ZonedDateTime sentAt = notification.getSentAt()
            .orElse(ZonedDateTime.now())
            .withZoneSameInstant(UK_TIMEZONE);
        return sentAt.toLocalDateTime().toString();
    }

    private String extractRecipient(Notification notification, String method) {
        return switch (method) {
            case "email" -> notification.getEmailAddress().orElse(NOT_AVAILABLE);
            case "sms" -> notification.getPhoneNumber().orElse(NOT_AVAILABLE);
            case "letter" -> buildAddressString(notification);
            default -> NOT_AVAILABLE;
        };
    }

    private String buildAddressString(Notification notification) {
        String address = Stream.of(
                notification.getLine1(),
                notification.getLine2(),
                notification.getLine3(),
                notification.getLine4(),
                notification.getLine5(),
                notification.getLine6()
            )
            .flatMap(Optional::stream)
            .filter(line -> !line.isBlank())
            .collect(Collectors.joining(", "));

        return address.isEmpty() ? NOT_AVAILABLE : address;
    }

    private String normalizeStatus(String status) {
        return FAILED_STATUSES.contains(status) ? "Failed" : StringUtils.capitalize(status);
    }

    @SuppressWarnings("checkstyle:AvoidEscapedUnicodeCharacters")
    private String sanitizeBody(String body) {
        String sanitized = body
                .replace("\r\n", "<br>")
                .replace("’", "'")
                .replace("‘", "'")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace(" ", " ")
                .replace("&nbsp;", " ");
        return "<div>" + sanitized + "</div>";
    }

    // Letter PDF handling

    public boolean isReferenceValidForLetterPdf(String notificationReference) {
        return VALID_REFERENCES.stream().anyMatch(notificationReference::contains);
    }

    public String getLetterEncodedPdfFile(
        String method,
        String notificationId,
        String notificationReference,
        Callback<AsylumCase> callback
    ) {
        if (!method.equalsIgnoreCase("letter") || !isReferenceValidForLetterPdf(notificationReference)) {
            return null;
        }
        return fetchAndEncodeLetterPdf(notificationId, callback);
    }

    private String fetchAndEncodeLetterPdf(String notificationId, Callback<AsylumCase> callback) {
        try {
            byte[] pdfFile = notificationClient.getPdfForLetter(notificationId);
            if (pdfFile != null && pdfFile.length > 0) {
                return Base64.getEncoder().encodeToString(pdfFile);
            }
        } catch (NotificationClientException e) {
            log.warn("Notification client getPdfForLetter failure on case {}: ",
                callback.getCaseDetails().getId(), e);
        }
        return null;
    }

    // Notification filtering

    private List<IdValue<StoredNotification>> removeDuplicateNotifications(
        List<IdValue<StoredNotification>> notifications
    ) {
        Set<String> seenIds = new HashSet<>();
        return notifications.stream()
            .filter(idValue -> seenIds.add(idValue.getValue().getNotificationId()))
            .collect(Collectors.toList());
    }

    private Set<String> findUnstoredNotificationIds(
        List<IdValue<StoredNotification>> storedNotifications,
        List<IdValue<String>> sentNotificationIds
    ) {
        Set<String> alreadyStoredIds = storedNotifications.stream()
            .map(idValue -> idValue.getValue().getNotificationId())
            .collect(Collectors.toSet());

        return sentNotificationIds.stream()
            .filter(this::wasSentWithinRetentionPeriod)
            .map(IdValue::getValue)
            .filter(id -> !alreadyStoredIds.contains(id))
            .collect(Collectors.toSet());
    }

    private boolean wasSentWithinRetentionPeriod(IdValue<String> idValue) {
        Matcher matcher = TIMESTAMP_PATTERN.matcher(idValue.getId());
        if (!matcher.find()) {
            return false;
        }

        long notificationTimestamp = Long.parseLong(matcher.group(1));
        Duration retentionPeriod = Duration.ofDays(configuration.getRetentionDays());
        long retentionCutoff = Instant.now().minus(retentionPeriod).toEpochMilli();
        return notificationTimestamp >= retentionCutoff;
    }

    private List<String> getNotificationIdsToUpdate(
        List<IdValue<StoredNotification>> storedNotifications,
        List<IdValue<String>> sentNotificationIds
    ) {
        return sentNotificationIds.stream()
            .filter(this::wasSentWithinRetentionPeriod)
            .filter(idValue -> doesStoredNotificationNeedUpdating(storedNotifications, idValue.getValue()))
            .map(IdValue::getValue)
            .toList();
    }

    private boolean doesStoredNotificationNeedUpdating(
        List<IdValue<StoredNotification>> storedNotifications,
        String notificationId
    ) {
        return storedNotifications.stream()
            .anyMatch(idValue -> idValue.getValue().getNotificationId().equals(notificationId)
                && !SUCCESSFUL_STATUSES.contains(idValue.getValue().getNotificationStatus()));
    }

    private List<IdValue<StoredNotification>> sortAndReindexNotificationsByDate(
        List<IdValue<StoredNotification>> allNotifications
    ) {
        allNotifications.sort(Comparator.comparing(notification ->
                LocalDateTime.parse(notification.getValue().getNotificationDateSent()),
            Comparator.reverseOrder()
        ));
        List<IdValue<StoredNotification>> updatedNotifications = new ArrayList<>();
        for (int i = 0; i < allNotifications.size(); i++) {
            updatedNotifications.add(new IdValue<>(String.valueOf(i + 1), allNotifications.get(i).getValue()));
        }
        return updatedNotifications;
    }

}
