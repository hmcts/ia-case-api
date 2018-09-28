package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Document;

public class DocumentWithMetadata {

    private Optional<Document> document = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<String> stored = Optional.empty();
    private Optional<String> dateUploaded = Optional.empty();

    private DocumentWithMetadata() {
        // noop -- for deserializer
    }

    public DocumentWithMetadata(
        Document document
    ) {
        this.document = Optional.ofNullable(document);
    }

    public DocumentWithMetadata(
        Document document,
        String description
    ) {
        this.document = Optional.ofNullable(document);
        this.description = Optional.ofNullable(description);
    }

    public DocumentWithMetadata(
        Document document,
        String description,
        String stored,
        String dateUploaded
    ) {
        this.document = Optional.ofNullable(document);
        this.description = Optional.ofNullable(description);
        this.stored = Optional.ofNullable(stored);
        this.dateUploaded = Optional.ofNullable(dateUploaded);
    }

    public Optional<Document> getDocument() {
        return document;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public Optional<String> getStored() {
        return stored;
    }

    public Optional<String> getDateUploaded() {
        return dateUploaded;
    }

    public void setDocument(Document document) {
        this.document = Optional.ofNullable(document);
    }

    public void setDescription(String description) {
        this.description = Optional.ofNullable(description);
    }

    public void setStored(Optional<String> stored) {
        this.stored = stored;
    }

    public void setDateUploaded(String dateUploaded) {
        this.dateUploaded = Optional.ofNullable(dateUploaded);
    }
}
