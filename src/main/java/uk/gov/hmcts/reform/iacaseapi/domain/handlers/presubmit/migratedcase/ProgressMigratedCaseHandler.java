package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.migratedcase;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNoteMigration;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADD_CASE_NOTES_MIGRATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_DESIRED_STATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.PROGRESS_MIGRATED_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;


@Slf4j
@Component
public class ProgressMigratedCaseHandler implements PreSubmitCallbackStateHandler<AsylumCase> {

    private final Appender<CaseNote> caseNoteAppender;
    private final DateProvider dateProvider;
    private final UserDetails userDetails;

    public ProgressMigratedCaseHandler(
        Appender<CaseNote> caseNoteAppender,
        DateProvider dateProvider,
        UserDetails userDetails
    ) {
        this.caseNoteAppender = caseNoteAppender;
        this.dateProvider = dateProvider;
        this.userDetails = userDetails;
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == ABOUT_TO_SUBMIT && callback.getEvent() == PROGRESS_MIGRATED_CASE;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase>
        callback, PreSubmitCallbackResponse<AsylumCase> callbackResponse) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        log.info("PreSubmitCallbackResponse ProgressMigratedCaseHandler for event: {}", callback.getEvent());


        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        addCaseNotes(asylumCase);

        State newDesiredState = asylumCase.read(ARIA_DESIRED_STATE, State.class)
            .orElseThrow(() -> new IllegalStateException("ariaDesiredState is not present"));

        return new PreSubmitCallbackResponse<>(asylumCase, newDesiredState);
    }

    private void addCaseNotes(AsylumCase asylumCase) {

        Optional<List<IdValue<CaseNoteMigration>>> maybeExistingCaseNotes =
            asylumCase.read(ADD_CASE_NOTES_MIGRATION);

        if (maybeExistingCaseNotes.isEmpty()) {
            return;
        }

        List<IdValue<CaseNoteMigration>> existingCaseNotes = maybeExistingCaseNotes.get();
        Collections.reverse(existingCaseNotes);

        List<CaseNote> newCaseNoteList = existingCaseNotes.stream()
            .map(existingCaseNote -> {
                CaseNoteMigration caseNoteMigration = existingCaseNote.getValue();
                CaseNote newCaseNote = new CaseNote(
                    caseNoteMigration.getCaseNoteSubject(),
                    caseNoteMigration.getCaseNoteDescription(),
                    buildFullName(),
                    dateProvider.now().toString()
                );
                if (caseNoteMigration.getCaseNoteDocument() != null) {
                    newCaseNote.setCaseNoteDocument(caseNoteMigration.getCaseNoteDocument());
                }
                return newCaseNote;
            })
            .toList();

        List<IdValue<CaseNote>> appendedCaseNoteList = new ArrayList<>();
        for (CaseNote newCaseNote : newCaseNoteList) {
            appendedCaseNoteList = caseNoteAppender.append(newCaseNote, appendedCaseNoteList);
        }

        asylumCase.write(CASE_NOTES, appendedCaseNoteList);
        asylumCase.clear(ADD_CASE_NOTES_MIGRATION);
    }

    private String buildFullName() {
        return userDetails.getForename()
            + " "
            + userDetails.getSurname();
    }
}
