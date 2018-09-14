package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Document;

public class WrittenLegalArgument {

    private Optional<Document> document = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<Documents> evidence = Optional.empty();

    private WrittenLegalArgument() {
        // noop -- for deserializer
    }

    public WrittenLegalArgument(
        Document document,
        String description,
        Documents evidence
    ) {
        this.document = Optional.ofNullable(document);
        this.description = Optional.ofNullable(description);
        this.evidence = Optional.ofNullable(evidence);
    }

    public Optional<Document> getDocument() {
        return document;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public Optional<Documents> getEvidence() {
        return evidence;
    }

    public void setDocument(Document document) {
        this.document = Optional.ofNullable(document);
    }

    public void setDescription(String description) {
        this.description = Optional.ofNullable(description);
    }

    public void setEvidence(Documents evidence) {
        this.evidence = Optional.ofNullable(evidence);
    }
}
