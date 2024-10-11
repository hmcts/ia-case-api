package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.HasDocument;

@EqualsAndHashCode
@ToString
public class DocumentWithMetadata implements HasDocument {

    private Document document;
    @Getter
    private String description;
    private String dateUploaded;
    private DocumentTag tag;
    @Getter
    private String suppliedBy;
    @Getter
    private String uploadedBy;
    @Setter
    @Getter
    private String dateTimeUploaded;

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
        this(document, description, dateUploaded, tag, suppliedBy, null, null);
    }

    public DocumentWithMetadata(
        Document document,
        String description,
        String dateUploaded,
        DocumentTag tag,
        String suppliedBy,
        String uploadedBy
    ) {
        this(document, description, dateUploaded, tag, suppliedBy, uploadedBy, null);
    }

    public DocumentWithMetadata(
            Document document,
            String description,
            String dateUploaded,
            DocumentTag tag,
            String suppliedBy,
            String uploadedBy,
            String dateTimeUploaded
    ) {
        this.document = document;
        this.description = description;
        this.dateUploaded = dateUploaded;
        this.tag = tag;
        this.suppliedBy = suppliedBy;
        this.uploadedBy = uploadedBy;
        this.dateTimeUploaded = dateTimeUploaded;
    }

    @Override
    public Document getDocument() {
        requireNonNull(document);
        return document;
    }

    public String getDateUploaded() {
        requireNonNull(dateUploaded);
        return dateUploaded;
    }

    public DocumentTag getTag() {
        requireNonNull(tag);
        return tag;
    }
}
