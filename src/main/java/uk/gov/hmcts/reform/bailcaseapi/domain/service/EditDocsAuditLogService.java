package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.EDIT_DOCUMENTS_REASON;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit.editdocs.AuditDetails;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam.IdamUserDetailsHelper;

@Component
public class EditDocsAuditLogService {

    @Autowired
    private UserDetails userDetails;

    @Autowired
    private IdamUserDetailsHelper idamUserDetailsHelper;

    @Autowired
    private EditDocsAuditService editDocsAuditService;

    public AuditDetails buildAuditDetails(long caseId, BailCase bailCase, BailCase bailCaseBefore) {
        return AuditDetails.builder()
            .caseId(caseId)
            .documentIds(getUpdatedAndDeletedAndAddedDocIds(bailCase, bailCaseBefore))
            .documentNames(getUpdatedAndDeletedAndAddedDocumentNames(bailCase, bailCaseBefore))
            .idamUserId(userDetails.getId())
            .user(idamUserDetailsHelper.getIdamUserName(userDetails))
            .reason(bailCase.read(EDIT_DOCUMENTS_REASON, String.class).orElse(null))
            .dateTime(LocalDateTime.now())
            .build();
    }

    private List<String> getUpdatedAndDeletedAndAddedDocumentNames(BailCase bailCase, BailCase bailCaseBefore) {
        if (bailCaseBefore == null) {
            return Collections.emptyList();
        }

        List<String> docNames = new ArrayList<>();
        getListOfDocumentFields().forEach(field -> {
            docNames.addAll(
                editDocsAuditService.getUpdatedAndDeletedDocNamesForGivenField(bailCase, bailCaseBefore, field));
            docNames.addAll(
                editDocsAuditService.getAddedDocNamesForGivenField(bailCase, bailCaseBefore, field));
        });

        return docNames;
    }

    private List<String> getUpdatedAndDeletedAndAddedDocIds(BailCase bailCase, BailCase bailCaseBefore) {
        if (bailCaseBefore == null) {
            return Collections.emptyList();
        }
        List<String> docIds = new ArrayList<>();
        getListOfDocumentFields().forEach(field -> {
            docIds.addAll(
                editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(bailCase, bailCaseBefore, field));
            docIds.addAll(
                editDocsAuditService.getAddedDocIdsForGivenField(bailCase, bailCaseBefore, field));
        });
        return docIds;
    }

    private List<BailCaseFieldDefinition> getListOfDocumentFields() {
        return Arrays.asList(
            TRIBUNAL_DOCUMENTS_WITH_METADATA,
            HOME_OFFICE_DOCUMENTS_WITH_METADATA,
            APPLICANT_DOCUMENTS_WITH_METADATA);
    }
}
