package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

@ToString
@EqualsAndHashCode
public class DocumentWithDescription {

    private Optional<Document> document = Optional.empty();
    private Optional<String> description = Optional.empty();

    private DocumentWithDescription() {
        // noop -- for deserializer
    }

    public DocumentWithDescription(
        Document document,
        String description
    ) {
        this.document = Optional.ofNullable(document);
        this.description = Optional.ofNullable(description);
    }

    public Optional<Document> getDocument() {
        requireNonNull(document);
        return document;
    }

    public Optional<String> getDescription() {
        requireNonNull(description);
        return description;
    }
}
