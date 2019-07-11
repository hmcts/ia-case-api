package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADD_CASE_NOTE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CaseNoteAppender;

@Component
public class AddCaseNoteHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final CaseNoteAppender caseNoteAppender;
    private final DateProvider dateProvider;
    private final UserDetailsProvider userDetailsProvider;

    public AddCaseNoteHandler(
        CaseNoteAppender caseNoteAppender,
        DateProvider dateProvider,
        @Qualifier("requestUser") UserDetailsProvider userDetailsProvider
    ) {
        this.caseNoteAppender = caseNoteAppender;
        this.dateProvider = dateProvider;
        this.userDetailsProvider = userDetailsProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_SUBMIT) && callback.getEvent().equals(ADD_CASE_NOTE);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        String caseNoteSubject = asylumCase
                .read(ADD_CASE_NOTE_SUBJECT, String.class)
                .orElseThrow(() -> new IllegalStateException("addCaseNoteSubject is not present"));

        String caseNoteDescription = asylumCase
                .read(ADD_CASE_NOTE_DESCRIPTION, String.class)
                .orElseThrow(() -> new IllegalStateException("addCaseNoteDescription is not present"));

        Optional<List<IdValue<CaseNote>>> maybeExistingCaseNotes =
            asylumCase.read(CASE_NOTES);

        Optional<Document> caseNoteDocument =
            asylumCase.read(ADD_CASE_NOTE_DOCUMENT, Document.class);

        final CaseNote newCaseNote = new CaseNote(
            caseNoteSubject,
            caseNoteDescription,
            buildFullName(),
            dateProvider.now().toString()
        );

        caseNoteDocument.ifPresent(newCaseNote::setCaseNoteDocument);

        List<IdValue<CaseNote>> allCaseNotes =
            caseNoteAppender.append(newCaseNote, maybeExistingCaseNotes.orElse(emptyList()));

        asylumCase.write(CASE_NOTES, allCaseNotes);

        asylumCase.clear(ADD_CASE_NOTE_SUBJECT);
        asylumCase.clear(ADD_CASE_NOTE_DESCRIPTION);
        asylumCase.clear(ADD_CASE_NOTE_DOCUMENT);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String buildFullName() {
        return userDetailsProvider.getUserDetails().getForename()
            + " "
            + userDetailsProvider.getUserDetails().getSurname();
    }
}
