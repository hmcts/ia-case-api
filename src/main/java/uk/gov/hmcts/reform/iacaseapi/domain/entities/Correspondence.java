package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Document;

public class Correspondence {

    private Optional<String> type = Optional.empty();
    private Optional<String> note = Optional.empty();
    private Optional<Document> document = Optional.empty();
    private Optional<String> correspondent = Optional.empty();
    private Optional<String> receivedDate = Optional.empty();

    private Correspondence() {
        // noop -- for deserializer
    }

    public Optional<String> getType() {
        return type;
    }

    public Optional<String> getNote() {
        return note;
    }

    public Optional<Document> getDocument() {
        return document;
    }

    public Optional<String> getCorrespondent() {
        return correspondent;
    }

    public Optional<String> getReceivedDate() {
        return receivedDate;
    }

    public void setType(String type) {
        this.type = Optional.ofNullable(type);
    }

    public void setNote(String note) {
        this.note = Optional.ofNullable(note);
    }

    public void setDocument(Document document) {
        this.document = Optional.ofNullable(document);
    }

    public void setCorrespondent(String correspondent) {
        this.correspondent = Optional.ofNullable(correspondent);
    }

    public void setReceivedDate(String receivedDate) {
        this.receivedDate = Optional.ofNullable(receivedDate);
    }
}
