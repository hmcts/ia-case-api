package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SourceOfRemittal;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

@Component
public class MarkAppealAsRemittedUploadDecisionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final Appender<CaseNote> caseNoteAppender;
    private final DateProvider dateProvider;

    public MarkAppealAsRemittedUploadDecisionHandler(Appender<CaseNote> caseNoteAppender,
                                                     DateProvider dateProvider) {
        this.caseNoteAppender = caseNoteAppender;
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.MARK_APPEAL_AS_REMITTED);
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

            Document mayBeDecisionDocument = asylumCase.read(UPLOAD_REMITTAL_DECISION_DOC, Document.class)
                .orElseThrow(() -> new IllegalStateException("uploadRemittalDecisionDoc is not present"));

            Document decisionDocumnent = new Document(mayBeDecisionDocument.getDocumentUrl(),
                mayBeDecisionDocument.getDocumentBinaryUrl(), getRemittalDecisionFilename(asylumCase));

            asylumCase.write(UPLOAD_REMITTAL_DECISION_DOC, decisionDocumnent);
            asylumCase.write(APPEAL_REMITTED_DATE, dateProvider.now().toString());
            asylumCase.write(REHEARING_REASON, "Remitted");
            asylumCase.write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YES);
            addRemittedCaseNote(asylumCase);


        return new PreSubmitCallbackResponse<>(asylumCase);
    }


    private String getRemittalDecisionFilename(AsylumCase asylumCase) {

        String courtReferenceNumber = asylumCase.read(COURT_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Court reference number not present"));

        return courtReferenceNumber
               + "-Decision-to-remit.pdf";
    }

    private void addRemittedCaseNote(AsylumCase asylumCase) {
        final SourceOfRemittal sourceOfRemittal = asylumCase.read(SOURCE_OF_REMITTAL, SourceOfRemittal.class)
            .orElseThrow(() -> new IllegalStateException("sourceOfRemittal is not present"));
        final String courtRef = asylumCase.read(COURT_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Court reference number not present"));
        final String judgesExcluded = asylumCase.read(JUDGES_NAMES_TO_EXCLUDE, String.class)
            .orElse("");
        final String additionalInst = asylumCase.read(REMITTED_ADDITIONAL_INSTRUCTIONS, String.class)
            .orElse("");

        String description = String.format("Reason for rehearing: Remitted" + System.lineSeparator() +
                                           "Remitted from: %s" + System.lineSeparator() +
                                           "%s reference: %s" + System.lineSeparator() +
                                           "Excluded judges: %s" + System.lineSeparator() +
                                           "Listing instructions: %s" + System.lineSeparator(),
            sourceOfRemittal, sourceOfRemittal, courtRef, judgesExcluded, additionalInst);

        final CaseNote newCaseNote = new CaseNote(
            "Appeal marked as remitted",
            description,
            "Admin",
            dateProvider.now().toString()
        );

        Optional<List<IdValue<CaseNote>>> maybeExistingCaseNotes = asylumCase.read(CASE_NOTES);
        List<IdValue<CaseNote>> allCaseNotes =
            caseNoteAppender.append(newCaseNote, maybeExistingCaseNotes.orElse(emptyList()));
        asylumCase.write(CASE_NOTES, allCaseNotes);

    }
}
