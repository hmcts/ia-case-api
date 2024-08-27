package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StoredNotification;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NOTIFICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NOTIFICATIONS_SENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SAVE_NOTIFICATIONS_TO_DATA;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SaveNotificationsToDataHandlerTest {

    @Mock
    private NotificationClient notificationClient;
    @Mock
    private Appender<StoredNotification> storedNotificationAppender;
    @Mock
    private Notification notification;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    private final String reference = "someReference";
    private final String notificationId = "someNotificationId";
    private final String body = "someBody";
    private final String notificationTypeEmail = "email";
    private final String notificationTypeSms = "sms";
    private final String status = "someStatus";
    private final String email = "some-email@test.com";
    private final String phoneNumber = "07827000000";
    private final String subject = "someSubject";
    private SaveNotificationsToDataHandler saveNotificationsToDataHandler;

    @BeforeEach
    void setUp() {
        when(callback.getEvent()).thenReturn(SAVE_NOTIFICATIONS_TO_DATA);
        saveNotificationsToDataHandler = new SaveNotificationsToDataHandler(
            notificationClient,
            storedNotificationAppender);
    }

    @Test
    void should_access_notify_client_if_missing_email_notification() throws NotificationClientException {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        List<IdValue<String>> notificationsSent =
            List.of(new IdValue<>(reference, notificationId));
        when(asylumCase.read(NOTIFICATIONS)).thenReturn(Optional.empty());
        when(asylumCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.of(notificationsSent));
        when(notificationClient.getNotificationById(notificationId)).thenReturn(notification);
        when(notification.getBody()).thenReturn(body);
        when(notification.getNotificationType()).thenReturn(notificationTypeEmail);
        when(notification.getEmailAddress()).thenReturn(Optional.of(email));
        when(notification.getReference()).thenReturn(Optional.of(reference));
        when(notification.getSubject()).thenReturn(Optional.of(subject));
        String dateString = "01-01-2024 10:57";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, dateFormatter);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Europe/London"));
        when(notification.getSentAt()).thenReturn(Optional.of(zonedDateTime));
        when(notification.getStatus()).thenReturn(status);
        saveNotificationsToDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(notificationClient, times(1)).getNotificationById(anyString());
        StoredNotification storedNotification =
            StoredNotification.builder()
                .notificationId(notificationId)
                .notificationDateSent("2024-01-01T10:57")
                .notificationSentTo(email)
                .notificationBody("<div>" + body + "</div>")
                .notificationMethod(StringUtils.capitalize(notificationTypeEmail))
                .notificationStatus(StringUtils.capitalize(status))
                .notificationReference(reference)
                .notificationSubject(subject)
                .build();
        verify(storedNotificationAppender, times(1)).append(storedNotification, emptyList());
        verify(asylumCase, times(1)).write(eq(NOTIFICATIONS), anyList());
    }

    @Test
    void should_access_notify_client_if_missing_sms_notification() throws NotificationClientException {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        List<IdValue<String>> notificationsSent =
            List.of(new IdValue<>(reference, notificationId));
        when(asylumCase.read(NOTIFICATIONS)).thenReturn(Optional.empty());
        when(asylumCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.of(notificationsSent));
        when(notificationClient.getNotificationById(notificationId)).thenReturn(notification);
        when(notification.getBody()).thenReturn(body);
        when(notification.getNotificationType()).thenReturn(notificationTypeSms);
        when(notification.getPhoneNumber()).thenReturn(Optional.of(phoneNumber));
        when(notification.getReference()).thenReturn(Optional.of(reference));
        String dateString = "01-01-2024 10:57";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, dateFormatter);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Europe/London"));
        when(notification.getSentAt()).thenReturn(Optional.of(zonedDateTime));
        when(notification.getStatus()).thenReturn(status);
        saveNotificationsToDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(notificationClient, times(1)).getNotificationById(anyString());
        StoredNotification storedNotification =
            StoredNotification.builder()
                .notificationId(notificationId)
                .notificationDateSent("2024-01-01T10:57")
                .notificationSentTo(phoneNumber)
                .notificationBody("<div>" + body + "</div>")
                .notificationMethod(StringUtils.capitalize(notificationTypeSms))
                .notificationStatus(StringUtils.capitalize(status))
                .notificationReference(reference)
                .notificationSubject("N/A")
                .build();
        verify(storedNotificationAppender, times(1)).append(storedNotification, emptyList());
        verify(asylumCase, times(1)).write(eq(NOTIFICATIONS), anyList());
    }

    @Test
    void should_access_set_reference_to_id_if_no_reference() throws NotificationClientException {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        List<IdValue<String>> notificationsSent =
            List.of(new IdValue<>(reference, notificationId));
        when(asylumCase.read(NOTIFICATIONS)).thenReturn(Optional.empty());
        when(asylumCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.of(notificationsSent));
        when(notificationClient.getNotificationById(notificationId)).thenReturn(notification);
        when(notification.getBody()).thenReturn(body);
        when(notification.getNotificationType()).thenReturn(notificationTypeEmail);
        when(notification.getEmailAddress()).thenReturn(Optional.of(email));
        when(notification.getSubject()).thenReturn(Optional.of(subject));
        String dateString = "01-01-2024 10:57";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, dateFormatter);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Europe/London"));
        when(notification.getSentAt()).thenReturn(Optional.of(zonedDateTime));
        when(notification.getStatus()).thenReturn(status);
        saveNotificationsToDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(notificationClient, times(1)).getNotificationById(anyString());
        StoredNotification storedNotification =
            StoredNotification.builder()
                .notificationId(notificationId)
                .notificationDateSent("2024-01-01T10:57")
                .notificationSentTo(email)
                .notificationBody("<div>" + body + "</div>")
                .notificationMethod(StringUtils.capitalize(notificationTypeEmail))
                .notificationStatus(StringUtils.capitalize(status))
                .notificationReference(notificationId)
                .notificationSubject(subject)
                .build();
        verify(storedNotificationAppender, times(1)).append(storedNotification, emptyList());
        verify(asylumCase, times(1)).write(eq(NOTIFICATIONS), anyList());
    }

    @Test
    void should_access_default_subject_if_none_found() throws NotificationClientException {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        List<IdValue<String>> notificationsSent =
            List.of(new IdValue<>(reference, notificationId));
        when(asylumCase.read(NOTIFICATIONS)).thenReturn(Optional.empty());
        when(asylumCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.of(notificationsSent));
        when(notificationClient.getNotificationById(notificationId)).thenReturn(notification);
        when(notification.getBody()).thenReturn(body);
        when(notification.getNotificationType()).thenReturn(notificationTypeEmail);
        when(notification.getEmailAddress()).thenReturn(Optional.of(email));
        when(notification.getSubject()).thenReturn(Optional.empty());
        String dateString = "01-01-2024 10:57";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, dateFormatter);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Europe/London"));
        when(notification.getSentAt()).thenReturn(Optional.of(zonedDateTime));
        when(notification.getStatus()).thenReturn(status);
        saveNotificationsToDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(notificationClient, times(1)).getNotificationById(anyString());
        StoredNotification storedNotification =
            StoredNotification.builder()
                .notificationId(notificationId)
                .notificationDateSent("2024-01-01T10:57")
                .notificationSentTo(email)
                .notificationBody("<div>" + body + "</div>")
                .notificationMethod(StringUtils.capitalize(notificationTypeEmail))
                .notificationStatus(StringUtils.capitalize(status))
                .notificationReference(notificationId)
                .notificationSubject("N/A")
                .build();
        verify(storedNotificationAppender, times(1)).append(storedNotification, emptyList());
        verify(asylumCase, times(1)).write(eq(NOTIFICATIONS), anyList());
    }


    @Test
    void should_access_default_sent_to_if_method_not_email_or_sms() throws NotificationClientException {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        List<IdValue<String>> notificationsSent =
            List.of(new IdValue<>(reference, notificationId));
        when(asylumCase.read(NOTIFICATIONS)).thenReturn(Optional.empty());
        when(asylumCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.of(notificationsSent));
        when(notificationClient.getNotificationById(notificationId)).thenReturn(notification);
        when(notification.getBody()).thenReturn(body);
        when(notification.getNotificationType()).thenReturn("unknownType");
        when(notification.getSubject()).thenReturn(Optional.empty());
        String dateString = "01-01-2024 10:57";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, dateFormatter);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Europe/London"));
        when(notification.getSentAt()).thenReturn(Optional.of(zonedDateTime));
        when(notification.getStatus()).thenReturn(status);
        saveNotificationsToDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(notificationClient, times(1)).getNotificationById(anyString());
        StoredNotification storedNotification =
            StoredNotification.builder()
                .notificationId(notificationId)
                .notificationDateSent("2024-01-01T10:57")
                .notificationSentTo("N/A")
                .notificationBody("<div>" + body + "</div>")
                .notificationMethod("UnknownType")
                .notificationStatus(StringUtils.capitalize(status))
                .notificationReference(notificationId)
                .notificationSubject("N/A")
                .build();
        verify(storedNotificationAppender, times(1)).append(storedNotification, emptyList());
        verify(asylumCase, times(1)).write(eq(NOTIFICATIONS), anyList());
    }

    @Test
    void should_not_access_notify_client_if_no_notifications_sent() throws NotificationClientException {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        StoredNotification storedNotification =
            StoredNotification.builder()
                .notificationId(notificationId)
                .notificationDateSent("2024-01-01T10:57")
                .notificationSentTo(email)
                .notificationBody("<div>" + body + "</div>")
                .notificationMethod(notificationTypeEmail)
                .notificationStatus(StringUtils.capitalize(status))
                .notificationReference(reference)
                .notificationSubject(subject)
                .build();
        List<IdValue<StoredNotification>> storedNotifications =
            List.of(new IdValue<>(reference, storedNotification));
        when(asylumCase.read(NOTIFICATIONS)).thenReturn(Optional.of(storedNotifications));
        when(asylumCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.empty());

        saveNotificationsToDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(notificationClient, times(0)).getNotificationById(anyString());
        verify(storedNotificationAppender, times(0))
            .append(any(StoredNotification.class), anyList());
        verify(asylumCase, times(1)).write(NOTIFICATIONS, storedNotifications);
    }

    @Test
    void should_not_access_notify_client_if_stored_notifications_match_notifications_sent() throws NotificationClientException {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        StoredNotification storedNotification =
            StoredNotification.builder()
                .notificationId(notificationId)
                .notificationDateSent("2024-01-01T10:57")
                .notificationSentTo(email)
                .notificationBody("<div>" + body + "</div>")
                .notificationMethod(notificationTypeEmail)
                .notificationStatus(StringUtils.capitalize(status))
                .notificationReference(reference)
                .notificationSubject(subject)
                .build();
        List<IdValue<StoredNotification>> storedNotifications =
            List.of(new IdValue<>(reference, storedNotification));
        List<IdValue<String>> notificationsSent =
            List.of(new IdValue<>(reference, notificationId));
        when(asylumCase.read(NOTIFICATIONS)).thenReturn(Optional.of(storedNotifications));
        when(asylumCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.of(notificationsSent));

        saveNotificationsToDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(notificationClient, times(0)).getNotificationById(anyString());
        verify(storedNotificationAppender, times(0))
            .append(any(StoredNotification.class), anyList());
        verify(asylumCase, times(1)).write(NOTIFICATIONS, storedNotifications);
    }

    @Test
    void should_not_break_function_if_notification_client_throws_exception() throws NotificationClientException {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        List<IdValue<String>> notificationsSent =
            List.of(new IdValue<>(reference, notificationId));
        when(asylumCase.read(NOTIFICATIONS)).thenReturn(Optional.empty());
        when(asylumCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.of(notificationsSent));
        when(notificationClient.getNotificationById(anyString()))
            .thenThrow(new NotificationClientException("some-client-error"));
        saveNotificationsToDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(storedNotificationAppender, times(0))
            .append(any(StoredNotification.class), anyList());
        verify(asylumCase, times(1)).write(NOTIFICATIONS, emptyList());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> saveNotificationsToDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> saveNotificationsToDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void it_can_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            boolean canHandle = saveNotificationsToDataHandler.canHandle(callbackStage, callback);

            if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && SAVE_NOTIFICATIONS_TO_DATA == callback.getEvent()) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
        }
    }

}