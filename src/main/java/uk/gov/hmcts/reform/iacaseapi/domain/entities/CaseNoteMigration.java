package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
public class CaseNoteMigration {

    private String caseNoteSubject;
    private String caseNoteDescription;
    @Getter
    private Document caseNoteDocument;

    public CaseNoteMigration() {
    }

    public CaseNoteMigration(
        String caseNoteSubject,
        String caseNoteDescription,
        Document caseNoteDocument
    ) {
        this.caseNoteSubject = requireNonNull(caseNoteSubject);
        this.caseNoteDescription = requireNonNull(caseNoteDescription);
        this.caseNoteDocument = caseNoteDocument;
    }

    public String getCaseNoteSubject() {
        return requireNonNull(caseNoteSubject);
    }

    public String getCaseNoteDescription() {
        return requireNonNull(caseNoteDescription);
    }


}
