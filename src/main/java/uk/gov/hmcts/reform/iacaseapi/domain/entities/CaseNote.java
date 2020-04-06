package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

@EqualsAndHashCode
@ToString
public class CaseNote {

    private String caseNoteSubject;
    private String caseNoteDescription;
    private Document caseNoteDocument;
    private String user;
    private String dateAdded;

    private CaseNote() {
    }

    public CaseNote(
        String caseNoteSubject,
        String caseNoteDescription,
        String user,
        String dateAdded
    ) {
        this.caseNoteSubject = requireNonNull(caseNoteSubject);
        this.caseNoteDescription = requireNonNull(caseNoteDescription);
        this.user = requireNonNull(user);
        this.dateAdded = requireNonNull(dateAdded);
    }

    public String getCaseNoteSubject() {
        return requireNonNull(caseNoteSubject);
    }

    public String getCaseNoteDescription() {
        return requireNonNull(caseNoteDescription);
    }

    public String getUser() {
        return requireNonNull(user);
    }

    public String getDateAdded() {
        return requireNonNull(dateAdded);
    }

    public Document getCaseNoteDocument() {
        return caseNoteDocument;
    }

    public void setCaseNoteDocument(Document document) {
        caseNoteDocument = document;
    }
}
