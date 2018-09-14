package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Document;

public class DocumentWithType {

    private Optional<Document> document = Optional.empty();
    private Optional<String> type = Optional.empty();

    public Optional<Document> getDocument() {
        return document;
    }

    public Optional<String> getType() {
        return type;
    }

    public void setDocument(Document document) {
        this.document = Optional.ofNullable(document);
    }

    public void setType(String type) {
        this.type = Optional.ofNullable(type);
    }
}
