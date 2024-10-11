package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

class StoredNotificationTest {

    private final String notificationId = "someId";
    private final String notificationDateSent = "someDateSent";
    private final String notificationSentTo = "someSentTo";
    private final String notificationMethod = "someMethod";
    private final String notificationStatus = "someStatus";
    private final String notificationBody = "someBody";
    private final String notificationReference = "someReference";
    private final String notificationSubject = "someSubject";
    private final Document document = mock(Document.class);

    private StoredNotification storedNotification;

    @BeforeEach
    public void setUp() {
        storedNotification =
            StoredNotification.builder()
                .notificationId(notificationId)
                .notificationDateSent(notificationDateSent)
                .notificationSentTo(notificationSentTo)
                .notificationBody(notificationBody)
                .notificationMethod(notificationMethod)
                .notificationStatus(notificationStatus)
                .notificationReference(notificationReference)
                .notificationSubject(notificationSubject)
                .build();
    }

    @Test
    void should_hold_onto_values() {
        assertThat(storedNotification.getNotificationId()).isEqualTo(notificationId);
        assertThat(storedNotification.getNotificationMethod()).isEqualTo(notificationMethod);
        assertThat(storedNotification.getNotificationSentTo()).isEqualTo(notificationSentTo);
        assertThat(storedNotification.getNotificationStatus()).isEqualTo(notificationStatus);
        assertThat(storedNotification.getNotificationBody()).isEqualTo(notificationBody);
        assertThat(storedNotification.getNotificationDateSent()).isEqualTo(notificationDateSent);
        assertThat(storedNotification.getNotificationReference()).isEqualTo(notificationReference);
        assertThat(storedNotification.getNotificationSubject()).isEqualTo(notificationSubject);
        assertThat(storedNotification.getNotificationDocument()).isNull();
        storedNotification.setNotificationDocument(document);
        assertThat(storedNotification.getNotificationDocument()).isEqualTo(document);
    }

    @Test
    void should_not_allow_null_arguments_other_than_document() {

        StoredNotification.StoredNotificationBuilder builder = StoredNotification.builder();
        assertThatThrownBy(() -> builder.notificationId(null)).isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> builder.notificationDateSent(null)).isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> builder.notificationSentTo(null)).isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> builder.notificationBody(null)).isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> builder.notificationMethod(null)).isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> builder.notificationStatus(null)).isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> builder.notificationReference(null)).isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> builder.notificationSubject(null)).isExactlyInstanceOf(NullPointerException.class);
    }
}
