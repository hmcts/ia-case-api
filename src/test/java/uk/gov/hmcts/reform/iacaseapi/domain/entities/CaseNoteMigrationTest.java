package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

@ExtendWith(MockitoExtension.class)
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
