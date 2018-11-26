package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

public class DocumentWithDescription {

    private Document document;
    private String description;

    private DocumentWithDescription() {
        // noop -- for deserializer
    }

    public DocumentWithDescription(
        Document document,
        String description
    ) {
        this.document = document;
        this.description = description;
    }

    public Document getDocument() {
        requireNonNull(document);
        return document;
    }

    public String getDescription() {
        requireNonNull(description);
        return description;
    }
}
