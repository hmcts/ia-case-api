package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.presubmit;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.CaseNotes;

@Component
public class CaseNoteUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.ABOUT_TO_SUBMIT
               && callback.getEventId() == EventId.ADD_CASE_NOTE;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> preSubmitResponse =
            new PreSubmitCallbackResponse<>(asylumCase);

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
