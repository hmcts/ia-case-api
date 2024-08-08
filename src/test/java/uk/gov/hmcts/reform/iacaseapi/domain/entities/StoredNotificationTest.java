package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class StoredNotificationTest {

    private final String notificationDateSent = "someDateSent";
    private final String notificationSentTo = "someSentTo";
    private final String notificationMethod = "someMethod";
    private final String notificationStatus = "someStatus";
    private final Document document = mock(Document.class);

    private StoredNotification storedNotification;

    @BeforeEach
    public void setUp() {
        storedNotification = new StoredNotification(
            notificationDateSent,
            notificationSentTo,
            document,
            notificationMethod,
            notificationStatus);
    }

    @Test
    void should_hold_onto_values() {

        assertThat(storedNotification.getNotificationMethod()).isEqualTo(notificationMethod);
        assertThat(storedNotification.getNotificationSentTo()).isEqualTo(notificationSentTo);
        assertThat(storedNotification.getNotificationStatus()).isEqualTo(notificationStatus);
        assertThat(storedNotification.getNotificationDateSent()).isEqualTo(notificationDateSent);
        assertThat(storedNotification.getNotificationDocument()).isEqualTo(document);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new StoredNotification(null, "", document, "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new StoredNotification("", null, document, "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new StoredNotification("", "", null, "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new StoredNotification("", "", document, null, ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new StoredNotification("", "", document, "", null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
