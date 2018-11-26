package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

public class DocumentWithMetadata {

    private Document document;
    private String description;
    private String dateUploaded;

    private DocumentWithMetadata() {
        // noop -- for deserializer
    }

    public DocumentWithMetadata(
        Document document,
        String description,
        String dateUploaded
    ) {
        this.document = document;
        this.description = description;
        this.dateUploaded = dateUploaded;
    }

    public Document getDocument() {
        requireNonNull(document);
        return document;
    }

    public String getDescription() {
        requireNonNull(description);
        return description;
    }

    public String getDateUploaded() {
        requireNonNull(dateUploaded);
        return dateUploaded;
    }
}
