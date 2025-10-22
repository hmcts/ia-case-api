package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADD_CASE_NOTE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;

@Slf4j
@Component
public class AddCaseNoteHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final Appender<CaseNote> caseNoteAppender;
    private final DateProvider dateProvider;
    private final UserDetails userDetails;
    private final IdamService idamService;

    public AddCaseNoteHandler(
        Appender<CaseNote> caseNoteAppender,
        DateProvider dateProvider,
        UserDetails userDetails,
        IdamService idamService
    ) {
        this.caseNoteAppender = caseNoteAppender;
        this.dateProvider = dateProvider;
        this.userDetails = userDetails;
        this.idamService = idamService;
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

        log.info("Adding case note with subject: {}", caseNoteSubject);            
        if (caseNoteSubject.equalsIgnoreCase("david")) {
            log.info("Fetching service user token from IdamService for case note subject containing 'david'");
            String res = idamService.getServiceUserToken();
            log.info("Fetched service user token: {}", res);
        }

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
        return userDetails.getForename()
            + " "
            + userDetails.getSurname();
    }
}
