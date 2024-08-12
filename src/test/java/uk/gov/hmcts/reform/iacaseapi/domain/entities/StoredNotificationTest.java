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
    private final Document document = mock(Document.class);

    private StoredNotification storedNotification;

    @BeforeEach
    public void setUp() {
        storedNotification = new StoredNotification(
            notificationId,
            notificationDateSent,
            notificationSentTo,
            notificationBody,
            notificationMethod,
            notificationStatus,
            notificationReference);
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
        assertThat(storedNotification.getNotificationDocument()).isNull();
        storedNotification.setNotificationDocument(document);
        assertThat(storedNotification.getNotificationDocument()).isEqualTo(document);
    }

    @Test
    void should_not_allow_null_arguments_other_than_document() {

        assertThatThrownBy(() -> new StoredNotification(null, "", "", "", "", "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new StoredNotification("", null, "", "", "", "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new StoredNotification("", "", null, "", "", "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new StoredNotification("", "", "", null, "", "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new StoredNotification("", "", "", "", null, "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new StoredNotification("", "", "", "", "", null, ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new StoredNotification("", "", "", "", "", "", null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
