package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.EDIT_DOCUMENTS_REASON;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.AuditDetails;

@Service
public class EditDocsCaseNoteService {

    @Autowired
    private Appender<CaseNote> appender;
    @Autowired
    private EditDocsAuditLogService editDocsAuditLogService;

    public void writeAuditCaseNoteForGivenCaseId(long caseId, BailCase bailCase, BailCase bailCaseBefore) {
        AuditDetails auditDetails = editDocsAuditLogService.buildAuditDetails(caseId, bailCase, bailCaseBefore);
        Optional<List<IdValue<CaseNote>>> maybeExistingCaseNotes = bailCase.read(CASE_NOTES);
        List<IdValue<CaseNote>> allCaseNotes = appender.append(
            buildNewCaseNote(auditDetails), maybeExistingCaseNotes.orElse(Collections.emptyList()));
        bailCase.write(CASE_NOTES, allCaseNotes);
        bailCase.clear(EDIT_DOCUMENTS_REASON);
    }

    private CaseNote buildNewCaseNote(AuditDetails auditDetails) {
        return new CaseNote(
            "A document was edited or deleted",
            getAuditDetailsFormatted(auditDetails),
            auditDetails.getUser(),
            LocalDate.now().toString()
        );
    }

    private String getAuditDetailsFormatted(AuditDetails auditDetails) {
        return String.format("Document names: %s" + System.lineSeparator() + "Reason: %s",
                             auditDetails.getDocumentNames(),
                             auditDetails.getReason()
        );
    }
}
