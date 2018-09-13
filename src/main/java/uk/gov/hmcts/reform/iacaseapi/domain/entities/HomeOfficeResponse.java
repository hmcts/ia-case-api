package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Document;

public class HomeOfficeResponse {

    private Optional<Document> document = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<Document> evidence = Optional.empty();
    private Optional<String> respondentName = Optional.empty();
    private Optional<String> responseDate = Optional.empty();

    public Optional<Document> getDocument() {
        return document;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public Optional<Document> getEvidence() {
        return evidence;
    }

    public Optional<String> getRespondentName() {
        return respondentName;
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

    public void setEvidence(Document evidence) {
        this.evidence = Optional.ofNullable(evidence);
    }

    public void setRespondentName(String respondentName) {
        this.respondentName = Optional.ofNullable(respondentName);
    }

    public void setResponseDate(String responseDate) {
        this.responseDate = Optional.ofNullable(responseDate);
    }
}
