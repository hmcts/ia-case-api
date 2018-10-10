package uk.gov.hmcts.reform.iacaseapi.shared.domain.entities;

import java.util.Optional;

public class HearingSummary {

    private Optional<DocumentWithMetadata> document = Optional.empty();

    public Optional<DocumentWithMetadata> getDocument() {
        return document;
    }

    public void setDocument(DocumentWithMetadata document) {
        this.document = Optional.ofNullable(document);
    }
}
