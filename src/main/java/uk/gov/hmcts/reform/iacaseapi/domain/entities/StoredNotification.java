package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
@Getter
public class StoredNotification {

    private String notificationDateSent;
    private String notificationSentTo;
    private Document notificationDocument;
    private String notificationMethod;
    private String notificationStatus;

    private StoredNotification() {
    }

    public StoredNotification(
        String notificationDateSent,
        String notificationSentTo,
        Document notificationDocument,
        String notificationMethod,
        String notificationStatus
    ) {
        this.notificationDateSent = requireNonNull(notificationDateSent);
        this.notificationSentTo = requireNonNull(notificationSentTo);
        this.notificationDocument = requireNonNull(notificationDocument);
        this.notificationMethod = requireNonNull(notificationMethod);
        this.notificationStatus = requireNonNull(notificationStatus);
    }

    public void setCaseNoteDocument(Document document) {
        notificationDocument = document;
    }
}
