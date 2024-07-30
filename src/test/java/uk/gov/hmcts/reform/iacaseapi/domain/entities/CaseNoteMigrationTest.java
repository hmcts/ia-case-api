package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

import static org.junit.jupiter.api.Assertions.*;

public class CaseNoteMigrationTest {
    @Mock
    private Document documentMock;

    @Test
    public void should_create_case_note_migration_with_valid_values() {
        String subject = "Test Subject";
        String description = "Test Description";

        CaseNoteMigration caseNoteMigration = new CaseNoteMigration(subject, description, documentMock);

        assertEquals(subject, caseNoteMigration.getCaseNoteSubject());
        assertEquals(description, caseNoteMigration.getCaseNoteDescription());
        assertEquals(documentMock, caseNoteMigration.getCaseNoteDocument());

    }
}
