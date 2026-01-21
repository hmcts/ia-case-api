package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;


@ExtendWith(MockitoExtension.class)
class CaseNoteAppenderTest {

    @Mock
    private CaseNote newCaseNote;
    @Mock
    private CaseNote oldCaseNote;
    @Mock
    private CaseNote oldestCaseNote;

    private Appender<CaseNote> caseNoteAppender;
    private List<IdValue<CaseNote>> oldCaseNotes = new ArrayList<>();

    @BeforeEach
    public void setUp() {

        oldCaseNotes.add(new IdValue<>(
            "2",
            oldCaseNote));

        oldCaseNotes.add(new IdValue<>(
            "1",
            oldestCaseNote));

        caseNoteAppender = new Appender<>();
    }

    @Test
    void appends_case_note_to_empty_list() {

        List<IdValue<CaseNote>> allCaseNotes = caseNoteAppender.append(newCaseNote, emptyList());

        assertThat(allCaseNotes)
            .extracting(IdValue::getValue)
            .containsOnly(newCaseNote);

        assertThat(allCaseNotes)
            .extracting(IdValue::getId)
            .containsOnly("1");
    }

    @Test
    void appends_case_note_to_existing_case_notes() {

        List<IdValue<CaseNote>> allCaseNotes = caseNoteAppender.append(newCaseNote, oldCaseNotes);

        assertThat(allCaseNotes)
            .extracting(IdValue::getValue)
            .containsExactly(newCaseNote, oldCaseNote, oldestCaseNote);

        assertThat(allCaseNotes)
            .extracting(IdValue::getId)
            .containsExactly("3", "2", "1");
    }

    @Test
    void throws_if_case_note_null() {

        assertThatThrownBy(() -> caseNoteAppender.append(null, oldCaseNotes))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
