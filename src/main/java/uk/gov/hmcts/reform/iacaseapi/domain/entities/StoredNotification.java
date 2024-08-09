package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
@Getter
public class StoredNotification {

    private String notificationId;
    private String notificationDateSent;
    private String notificationSentTo;
    private String notificationBody;
    @Setter
    private Document notificationDocument;
    private String notificationMethod;
    private String notificationStatus;

    private StoredNotification() {
    }

    public StoredNotification(
        String notificationId,
        String notificationDateSent,
        String notificationSentTo,
        String notificationBody,
        Document notificationDocument,
        String notificationMethod,
        String notificationStatus
    ) {
        this.notificationId = requireNonNull(notificationId);
        this.notificationDateSent = requireNonNull(notificationDateSent);
        this.notificationSentTo = requireNonNull(notificationSentTo);
        this.notificationBody = requireNonNull(notificationBody);
        this.notificationDocument = notificationDocument;
        this.notificationMethod = requireNonNull(notificationMethod);
        this.notificationStatus = requireNonNull(notificationStatus);
    }
}
