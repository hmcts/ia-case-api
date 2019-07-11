package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

public class CaseNoteTest {

    private final String caseNoteSubject = "some-subject";
    private final String caseNoteDescription = "some-description";
    private final String user = "some-user";
    private final String dateAdded = "some-date";
    private final Document caseNoteDocument = mock(Document.class);

    private CaseNote caseNote;

    @Before
    public void setUp() {
        caseNote = new CaseNote(
            caseNoteSubject,
            caseNoteDescription,
            user,
            dateAdded);

        caseNote.setCaseNoteDocument(caseNoteDocument);
    }

    @Test
    public void should_hold_onto_values() {

        assertThat(caseNote.getCaseNoteSubject()).isEqualTo(caseNoteSubject);
        assertThat(caseNote.getCaseNoteDescription()).isEqualTo(caseNoteDescription);
        assertThat(caseNote.getUser()).isEqualTo(user);
        assertThat(caseNote.getCaseNoteDocument()).isEqualTo(caseNoteDocument);
        assertThat(caseNote.getDateAdded()).isEqualTo(dateAdded);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new CaseNote(null, "", "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new CaseNote("", null, "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new CaseNote("", "", null, ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new CaseNote("", "", "", null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

}