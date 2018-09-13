package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Document;

public class HearingSummary {

    private Optional<Document> document = Optional.empty();
    private Optional<String> description = Optional.empty();

    public Optional<Document> getDocument() {
        return document;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public void setDocument(Document document) {
        this.document = Optional.ofNullable(document);
    }

    public void setDescription(String description) {
        this.description = Optional.ofNullable(description);
    }
}
