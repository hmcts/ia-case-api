package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NOTIFICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.TEST_NOTIFICATION_ID;
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

    private SaveNotificationsToDataHandler saveNotificationsToDataHandler;

    @BeforeEach
    void setUp() throws NotificationClientException {
        when(callback.getEvent()).thenReturn(SAVE_NOTIFICATIONS_TO_DATA);
        saveNotificationsToDataHandler = new SaveNotificationsToDataHandler(
            notificationClient,
            storedNotificationAppender);
    }

    @Test
    void should_access_notify_client_if_id_present() throws NotificationClientException {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(notificationClient.getNotificationById(anyString())).thenReturn(notification);
        when(asylumCase.read(TEST_NOTIFICATION_ID, String.class))
            .thenReturn(Optional.of("some-id"));
        when(notification.getBody()).thenReturn("someBody");
        when(notification.getId()).thenReturn(UUID.randomUUID());
        when(notification.getNotificationType()).thenReturn("someNotificationType");
        when(notification.getCompletedAt()).thenReturn(Optional.empty());
        when(notification.getCreatedByName()).thenReturn(Optional.empty());
        when(notification.getEstimatedDelivery()).thenReturn(Optional.empty());
        when(notification.getLine1()).thenReturn(Optional.empty());
        when(notification.getLine2()).thenReturn(Optional.empty());
        when(notification.getLine3()).thenReturn(Optional.empty());
        when(notification.getLine4()).thenReturn(Optional.empty());
        when(notification.getLine5()).thenReturn(Optional.empty());
        when(notification.getLine6()).thenReturn(Optional.empty());
        when(notification.getEmailAddress()).thenReturn(Optional.of("some-email@test.com"));
        when(notification.getPhoneNumber()).thenReturn(Optional.empty());
        when(notification.getPostage()).thenReturn(Optional.empty());
        when(notification.getPostcode()).thenReturn(Optional.empty());
        when(notification.getReference()).thenReturn(Optional.of("some-reference"));
        String dateString = "01-01-2024";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.parse(dateString, dateFormatter);
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
        when(notification.getSentAt()).thenReturn(Optional.of(zonedDateTime));
        when(notification.getSubject()).thenReturn(Optional.empty());
        when(notification.getStatus()).thenReturn("someStatus");
        Document document = new Document("", "", "some-reference.pdf");
        saveNotificationsToDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(notificationClient, times(1)).getNotificationById(anyString());
        StoredNotification storedNotification =
            new StoredNotification("2024-01-01T00:00Z[Europe/London]", "some-email@test.com",
                document, "someNotificationType", "someStatus");
        verify(storedNotificationAppender, times(1)).append(storedNotification, Collections.emptyList());
        verify(asylumCase, times(1)).write(eq(NOTIFICATIONS), anyList());

        when(notification.getEmailAddress()).thenReturn(Optional.empty());
        when(notification.getPhoneNumber()).thenReturn(Optional.of("07827201234"));
        saveNotificationsToDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(notificationClient, times(2)).getNotificationById(anyString());
        storedNotification =
            new StoredNotification("2024-01-01T00:00Z[Europe/London]", "07827201234",
                document, "someNotificationType", "someStatus");
        verify(storedNotificationAppender, times(1)).append(storedNotification, Collections.emptyList());
        verify(asylumCase, times(2)).write(eq(NOTIFICATIONS), anyList());

        when(notification.getPhoneNumber()).thenReturn(Optional.empty());
        saveNotificationsToDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(notificationClient, times(3)).getNotificationById(anyString());
        storedNotification =
            new StoredNotification("2024-01-01T00:00Z[Europe/London]", "N/A",
                document, "someNotificationType", "someStatus");
        verify(storedNotificationAppender, times(1)).append(storedNotification, Collections.emptyList());
        verify(asylumCase, times(3)).write(eq(NOTIFICATIONS), anyList());
    }

    @Test
    void should_not_access_notify_client_if_id_present() throws NotificationClientException {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(TEST_NOTIFICATION_ID, String.class))
            .thenReturn(Optional.empty());
        saveNotificationsToDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(notificationClient, times(0)).getNotificationById(anyString());
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