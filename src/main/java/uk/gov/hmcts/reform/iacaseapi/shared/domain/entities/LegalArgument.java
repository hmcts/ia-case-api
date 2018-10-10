package uk.gov.hmcts.reform.iacaseapi.shared.domain.entities;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.Document;

public class LegalArgument {

    private Optional<Document> document = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<Documents> evidence = Optional.empty();

    private LegalArgument() {
        // noop -- for deserializer
    }

    public LegalArgument(
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
