package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

    @ParameterizedTest
    @CsvSource(value = {
        "null,,,,,,,",
        ",null,,,,,,",
        ",,null,,,,,",
        ",,,null,,,,",
        ",,,,null,,,",
        ",,,,,null,,",
        ",,,,,,null,",
        ",,,,,,,null",
    })
    void should_not_allow_null_arguments_other_than_document(String id, String date, String sentTo, String body,
                                                             String method, String status, String reference, String subject) {
        assertThatThrownBy(() -> StoredNotification.builder()
            .notificationId(id)
            .notificationDateSent(date)
            .notificationSentTo(sentTo)
            .notificationBody(body)
            .notificationMethod(method)
            .notificationStatus(status)
            .notificationReference(reference)
            .notificationSubject(subject)
            .build())
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
