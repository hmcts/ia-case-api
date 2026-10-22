package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDENDUM_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ALL_FTPA_APPELLANT_DECISION_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ALL_FTPA_RESPONDENT_DECISION_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DRAFT_DECISION_AND_REASONS_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EDIT_DOCUMENTS_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FINAL_DECISION_AND_REASONS_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_RECORDING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REHEARD_HEARING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.TRIBUNAL_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_SENSITIVE_DOCS;

@Component
public class EditDocsAuditLogService {

    @Autowired
    private UserDetails userDetails;

    @Autowired
    private EditDocsAuditService editDocsAuditService;

    public AuditDetails buildAuditDetails(long caseId, AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        return AuditDetails.builder()
            .caseId(caseId)
            .documentIds(getDeletedDocIds(asylumCase, asylumCaseBefore))
            .documentNames(getDeletedDocumentNames(asylumCase, asylumCaseBefore))
            .idamUserId(userDetails.getId())
            .user(getIdamUserName(userDetails))
            .reason(asylumCase.read(EDIT_DOCUMENTS_REASON, String.class).orElse(null))
            .dateTime(LocalDateTime.now())
            .build();
    }

    private List<String> getDeletedDocumentNames(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        if (asylumCaseBefore == null) {
            return Collections.emptyList();
        }
        List<String> docNames = new ArrayList<>();
        getListOfDocumentFields().forEach(field -> docNames.addAll(
            editDocsAuditService.getUpdatedAndDeletedDocNamesForGivenField(asylumCase, asylumCaseBefore, field)));
        return docNames;
    }

    private String getIdamUserName(UserDetails userDetails) {
        return userDetails.getForename() + " " + userDetails.getSurname();
    }

    private List<String> getDeletedDocIds(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        if (asylumCaseBefore == null) {
            return Collections.emptyList();
        }
        List<String> docIds = new ArrayList<>();
        getListOfDocumentFields().forEach(field -> docIds.addAll(
            editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(asylumCase, asylumCaseBefore, field)));
        return docIds;
    }

    public List<String> getUploadedOrGeneratedDocumentNames(Callback<AsylumCase> callback) {
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        AsylumCase asylumCaseBefore = callback.getCaseDetailsBefore().map(CaseDetails::getCaseData).orElse(null);

        List<String> docNames = new ArrayList<>();
        getListOfDocumentFields().forEach(field -> docNames.addAll(
            editDocsAuditService.getUploadedOrGeneratedDocNamesForGivenField(asylumCase, asylumCaseBefore, field)));
        return docNames;
    }

    public List<AsylumCaseFieldDefinition> getListOfDocumentFields() {
        return Arrays.asList(
            ADDITIONAL_EVIDENCE_DOCUMENTS,
            TRIBUNAL_DOCUMENTS,
            REHEARD_HEARING_DOCUMENTS,
            HEARING_DOCUMENTS,
            LEGAL_REPRESENTATIVE_DOCUMENTS,
            ADDENDUM_EVIDENCE_DOCUMENTS,
            RESPONDENT_DOCUMENTS,
            DRAFT_DECISION_AND_REASONS_DOCUMENTS,
            FINAL_DECISION_AND_REASONS_DOCUMENTS,
            HEARING_RECORDING_DOCUMENTS,
            UPLOAD_SENSITIVE_DOCS,
            ALL_FTPA_APPELLANT_DECISION_DOCS,
            FTPA_APPELLANT_DOCUMENTS,
            ALL_FTPA_RESPONDENT_DECISION_DOCS,
            FTPA_RESPONDENT_DOCUMENTS
        );
    }
}
