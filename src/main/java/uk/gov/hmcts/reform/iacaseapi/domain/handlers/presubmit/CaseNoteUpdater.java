package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNotes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class CaseNoteUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.ADD_CASE_NOTE;
    }

    public CcdEventPreSubmitResponse<AsylumCase> handle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        if (!canHandle(stage, ccdEvent)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            ccdEvent
                .getCaseDetails()
                .getCaseData();

        CcdEventPreSubmitResponse<AsylumCase> preSubmitResponse =
            new CcdEventPreSubmitResponse<>(asylumCase);

        String caseNoteType = asylumCase
            .getCaseNoteType()
            .orElseThrow(() -> new IllegalStateException("caseNoteType not present"));

        String caseNoteNote = asylumCase
            .getCaseNoteNote()
            .orElseThrow(() -> new IllegalStateException("caseNoteNote not present"));

        Document caseNoteDocument = asylumCase
            .getCaseNoteDocument()
            .orElse(null);

        String caseNoteCorrespondent = asylumCase
            .getCaseNoteCorrespondent()
            .orElse(null);

        String caseNoteCorrespondenceDate = asylumCase
            .getCaseNoteCorrespondenceDate()
            .orElse(null);

        List<IdValue<CaseNote>> allCaseNotes = new ArrayList<>();

        CaseNotes caseNotes =
            asylumCase
                .getCaseNotes()
                .orElse(new CaseNotes());

        if (caseNotes.getCaseNotes().isPresent()) {
            allCaseNotes.addAll(
                caseNotes.getCaseNotes().get()
            );
        }

        allCaseNotes.add(
            new IdValue<>(
                String.valueOf(Instant.now().toEpochMilli()),
                new CaseNote(
                    caseNoteType,
                    caseNoteNote,
                    caseNoteDocument,
                    caseNoteCorrespondent,
                    caseNoteCorrespondenceDate,
                    LocalDate.now().toString()
                )
            )
        );

        caseNotes.setCaseNotes(allCaseNotes);

        asylumCase.setCaseNotes(caseNotes);

        asylumCase.clearCaseNote();

        return preSubmitResponse;
    }
}
