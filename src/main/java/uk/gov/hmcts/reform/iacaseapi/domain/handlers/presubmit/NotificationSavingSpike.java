package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.NotificationList;

public class NotificationSavingSpike {
    public static void main(String[] args) throws NotificationClientException {
        long startTime = System.nanoTime();
        LocalDateTime localDateTime = LocalDateTime.of(2025, 8, 15, 14, 0, 0);
        Map<String, Set<Notification>> notifications = notificationThingV3(localDateTime);
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        System.out.println("Total time taken: " + duration / 1_000_000 + " ms");
    }

    private static Map<String, Set<Notification>> notificationThingV3(LocalDateTime localDateTime) throws NotificationClientException {
        ZonedDateTime oldestNotificationDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("Z"));
        String oldestId = null;
        Map<String, Set<Notification>> notificationsByCaseRef = new HashMap<>();
        NotificationClient notificationClient = new NotificationClient("");
        int totalNotifications = 0;
        boolean finished = false;

        while (!finished) {
            NotificationList notificationList = notificationClient.getNotifications(null, null, null, oldestId);
            List<Notification> notifications = notificationList.getNotifications();
            for (Notification notification : notifications) {
                if (notification.getCreatedAt().isBefore(oldestNotificationDateTime)) {
                    finished = true;
                    break;
                }
                String caseRef = notification.getReference().orElse("").split("_", 2)[0];
                if (caseRef.length() != 16) {
                    continue; // Skip notifications without a case reference
                }
                totalNotifications++;
                notificationsByCaseRef.computeIfAbsent(caseRef, k -> new HashSet<>()).add(notification);
            }
            if (!notifications.isEmpty()) {
                oldestId = String.valueOf(notifications.get(notifications.size() - 1).getId());
            }
        }
        System.out.println("Total notifications so far: " + totalNotifications);
        return notificationsByCaseRef;
    }
}