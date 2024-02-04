package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EDIT_DOCUMENTS_REASON;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.AuditDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.EditDocsAuditLogService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

@Service
@Slf4j
public class EditDocsCaseNoteService {

    @Autowired
    private Appender<CaseNote> appender;
    @Autowired
    private EditDocsAuditLogService editDocsAuditLogService;

    public void writeAuditCaseNoteForGivenCaseId(long caseId, AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        AuditDetails auditDetails = editDocsAuditLogService.buildAuditDetails(caseId, asylumCase, asylumCaseBefore);
        Optional<List<IdValue<CaseNote>>> maybeExistingCaseNotes = asylumCase.read(CASE_NOTES);
        List<IdValue<CaseNote>> allCaseNotes = appender.append(
            buildNewCaseNote(auditDetails), maybeExistingCaseNotes.orElse(Collections.emptyList()));
        asylumCase.write(CASE_NOTES, allCaseNotes);
        asylumCase.clear(EDIT_DOCUMENTS_REASON);
        log.info("EditDocsCaseNoteService: ");
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
        return String.format("Document names: %s" + System.lineSeparator() + "reason: %s",
            auditDetails.getDocumentNames(),
            auditDetails.getReason()
        );
    }
}