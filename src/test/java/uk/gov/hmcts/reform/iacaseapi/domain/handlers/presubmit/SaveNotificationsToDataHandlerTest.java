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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NOTIFICATIONS;
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
        when(notification.getBody()).thenReturn("someBody");
        when(notification.getNotificationType()).thenReturn("someNotificationType");
        when(notification.getEmailAddress()).thenReturn(Optional.of("some-email@test.com"));
        String dateString = "01-01-2024";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.parse(dateString, dateFormatter);
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.of("Europe/London"));
        when(notification.getSentAt()).thenReturn(Optional.of(zonedDateTime));
        when(notification.getStatus()).thenReturn("someStatus");
        saveNotificationsToDataHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(notificationClient, times(2)).getNotificationById(anyString());
        StoredNotification storedNotification1 =
            new StoredNotification("f09f3d6b-2011-420d-9f2a-f3ee528c21c3", "2024-01-01", "some-email@test.com",
                "<div>someBody</div>", null, "someNotificationType", "someStatus");
        verify(storedNotificationAppender, times(1)).append(storedNotification1, Collections.emptyList());
        StoredNotification storedNotification2 =
            new StoredNotification("bd4d807e-bd2c-48d1-bef3-fac37785c05b", "2024-01-01", "some-email@test.com",
                "<div>someBody</div>", null, "someNotificationType", "someStatus");
        verify(storedNotificationAppender, times(1)).append(storedNotification1, Collections.emptyList());
        verify(asylumCase, times(1)).write(eq(NOTIFICATIONS), anyList());
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