package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Document;

public class HomeOfficeResponse {

    private Optional<Document> document = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<Documents> evidence = Optional.empty();
    private Optional<String> responseDate = Optional.empty();

    private HomeOfficeResponse() {
        // noop -- for deserializer
    }

    public HomeOfficeResponse(
        Document document,
        String description,
        Documents evidence,
        String responseDate
    ) {
        this.document = Optional.ofNullable(document);
        this.description = Optional.ofNullable(description);
        this.evidence = Optional.ofNullable(evidence);
        this.responseDate = Optional.ofNullable(responseDate);
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

    public Optional<String> getResponseDate() {
        return responseDate;
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

    public void setResponseDate(String responseDate) {
        this.responseDate = Optional.ofNullable(responseDate);
    }
}
