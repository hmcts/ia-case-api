package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class CaseNoteAppender {

    public List<IdValue<CaseNote>> append(
        CaseNote newCaseNote,
        List<IdValue<CaseNote>> existingCaseNotes
    ) {

        requireNonNull(newCaseNote);

        final List<IdValue<CaseNote>> allCaseNotes = new ArrayList<>();

        int index = existingCaseNotes.size() + 1;

        IdValue<CaseNote> caseNoteIdValue = new IdValue<>(String.valueOf(index--), newCaseNote);

        allCaseNotes.add(caseNoteIdValue);

        for (IdValue<CaseNote> existingCaseNote : existingCaseNotes) {
            allCaseNotes.add(new IdValue<>(
                String.valueOf(index--),
                existingCaseNote.getValue()));
        }

        return allCaseNotes;
    }
}
