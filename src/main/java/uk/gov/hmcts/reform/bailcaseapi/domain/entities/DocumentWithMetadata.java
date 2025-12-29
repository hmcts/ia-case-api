package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.HasDocument;

@EqualsAndHashCode
@ToString
public class DocumentWithMetadata implements HasDocument {

    private Document document;
    private String description;
    private String dateUploaded;
    private DocumentTag tag;
    private String suppliedBy;

    private DocumentWithMetadata() {
        // noop -- for deserializer
    }

    public DocumentWithMetadata(
        Document document,
        String description,
        String dateUploaded,
        DocumentTag tag
    ) {
        this(document, description, dateUploaded, tag, null);
    }

    public DocumentWithMetadata(
        Document document,
        String description,
        String dateUploaded,
        DocumentTag tag,
        String suppliedBy
    ) {
        this.document = document;
        this.description = description;
        this.dateUploaded = dateUploaded;
        this.tag = tag;
        this.suppliedBy = suppliedBy;
    }

    @Override
    public Document getDocument() {
        requireNonNull(document);
        return document;
    }

    public String getDescription() {
        return description;
    }

    public String getDateUploaded() {
        requireNonNull(dateUploaded);
        return dateUploaded;
    }

    public DocumentTag getTag() {
        requireNonNull(tag);
        return tag;
    }

    public String getSuppliedBy() {
        return suppliedBy;
    }

    public void setSuppliedBy(String newSuppliedBy) {
        this.suppliedBy = newSuppliedBy;
    }
}
