package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Document;

public class CaseNote {

    private Optional<String> type = Optional.empty();
    private Optional<String> note = Optional.empty();
    private Optional<Document> document = Optional.empty();
    private Optional<String> correspondent = Optional.empty();
    private Optional<String> correspondenceDate = Optional.empty();
    private Optional<String> addedDate = Optional.empty();

    private CaseNote() {
        // noop -- for deserializer
    }

    public CaseNote(
        String type,
        String note,
        Document document,
        String correspondent,
        String correspondenceDate,
        String addedDate
    ) {
        this.type = Optional.ofNullable(type);
        this.note = Optional.ofNullable(note);
        this.document = Optional.ofNullable(document);
        this.correspondent = Optional.ofNullable(correspondent);
        this.correspondenceDate = Optional.ofNullable(correspondenceDate);
        this.addedDate = Optional.ofNullable(addedDate);
    }

    public Optional<String> getType() {
        return type;
    }

    public Optional<String> getNote() {
        return note;
    }

    public Optional<Document> getDocument() {
        return document;
    }

    public Optional<String> getCorrespondent() {
        return correspondent;
    }

    public Optional<String> getCorrespondenceDate() {
        return correspondenceDate;
    }

    public Optional<String> getAddedDate() {
        return addedDate;
    }

    public void setType(String type) {
        this.type = Optional.ofNullable(type);
    }

    public void setNote(String note) {
        this.note = Optional.ofNullable(note);
    }

    public void setDocument(Document document) {
        this.document = Optional.ofNullable(document);
    }

    public void setCorrespondent(String correspondent) {
        this.correspondent = Optional.ofNullable(correspondent);
    }

    public void setCorrespondenceDate(String correspondenceDate) {
        this.correspondenceDate = Optional.ofNullable(correspondenceDate);
    }

    public void setAddedDate(String addedDate) {
        this.addedDate = Optional.ofNullable(addedDate);
    }
}
