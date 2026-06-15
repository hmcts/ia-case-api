package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.EditDocsAuditLogService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_NLR_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

@Component
public class AipNlrEventSubmissionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    @Autowired
    private Appender<CaseNote> appender;
    @Autowired
    private EditDocsAuditLogService editDocsAuditLogService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LAST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && asylumCase.read(HAS_NLR_SUBMITTED, YesOrNo.class).orElse(NO).equals(YesOrNo.YES));
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        asylumCase.clear(HAS_NLR_SUBMITTED);

        String nlrFullName = asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)
                .map(nlr -> nlr.getGivenNames() + " " + nlr.getFamilyName())
                .orElseThrow(() -> new IllegalStateException("Non-legal representative details are not present"));
        Optional<List<IdValue<CaseNote>>> maybeExistingCaseNotes = asylumCase.read(CASE_NOTES);
        List<IdValue<CaseNote>> allCaseNotes = appender.append(
            buildNewCaseNote(callback, nlrFullName), maybeExistingCaseNotes.orElse(Collections.emptyList()));
        asylumCase.write(CASE_NOTES, allCaseNotes);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private CaseNote buildNewCaseNote(Callback<AsylumCase> callback, String nlrFullName) {
        List<String> docList = editDocsAuditLogService.getUploadedOrGeneratedDocumentNames(callback);
        String caseNoteDescription = "Non-legal representative submitted the event on behalf of the appellant.";
        if (!docList.isEmpty()) {
            caseNoteDescription += "\nThe following documents were uploaded or generated as a result of this event:\n"
                + String.join(",\n", docList);
        }
        return new CaseNote(
            "NLR event submission - " + callback.getEvent().toString(),
            caseNoteDescription,
            nlrFullName,
            LocalDate.now().toString()
        );
    }
}
