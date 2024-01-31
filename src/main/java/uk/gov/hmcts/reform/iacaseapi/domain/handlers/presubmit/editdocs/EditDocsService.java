package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.EditDocsAuditService.getIdFromDocUrl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.EditDocsAuditService;

@Slf4j
@Service
public class EditDocsService {

    @Autowired
    private EditDocsAuditService docsAuditService;

    public void cleanUpOverviewTabDocs(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        List<String> deletedFinalDecisionAndReasonsDocIds = getDeletedDocIds(asylumCase, asylumCaseBefore);
        List<String> deletedFtpaDecisionDocIds = getDeletedFtpaDocIds(asylumCase, asylumCaseBefore);

        cleanUpDocumentList(asylumCase, FINAL_DECISION_AND_REASONS_PDF, deletedFinalDecisionAndReasonsDocIds);

        cleanUpFTPADocumentList(asylumCase, FTPA_APPELLANT_DECISION_DOCUMENT, deletedFtpaDecisionDocIds);
        cleanUpFTPADocumentList(asylumCase, FTPA_RESPONDENT_DECISION_DOCUMENT, deletedFtpaDecisionDocIds);
        cleanUpFTPADocumentList(asylumCase, FTPA_APPELLANT_GROUNDS_DOCUMENTS, deletedFtpaDecisionDocIds);
        cleanUpFTPADocumentList(asylumCase, FTPA_RESPONDENT_GROUNDS_DOCUMENTS, deletedFtpaDecisionDocIds);
        cleanUpFTPADocumentList(asylumCase, FTPA_APPELLANT_EVIDENCE_DOCUMENTS, deletedFtpaDecisionDocIds);
        cleanUpFTPADocumentList(asylumCase, FTPA_RESPONDENT_EVIDENCE_DOCUMENTS, deletedFtpaDecisionDocIds);
        updateFtpaDecision(asylumCase, ALL_FTPA_APPELLANT_DECISION_DOCS, FTPA_APPELLANT_DECISION_DOCUMENT);
        updateFtpaDecision(asylumCase, ALL_FTPA_RESPONDENT_DECISION_DOCS, FTPA_RESPONDENT_DECISION_DOCUMENT);
    }

    private void cleanUpDocumentList(AsylumCase asylumCase, AsylumCaseFieldDefinition documentType, List<String> deletedDocIds) {
        Document currentDocument = asylumCase.read(documentType, Document.class).orElse(null);
        if (currentDocument != null && doWeHaveToCleanUpOverviewTabDoc(deletedDocIds, currentDocument)) {
            asylumCase.clear(documentType);
        }
    }

    private void cleanUpFTPADocumentList(AsylumCase asylumCase, AsylumCaseFieldDefinition documentType, List<String> deletedFtpaDecisionDocIds) {
        Optional<List<IdValue<DocumentWithDescription>>> currentDocuments = asylumCase.read(documentType);
        currentDocuments.ifPresent(documentWithDescriptionList -> {
            documentWithDescriptionList.removeIf(idValue ->
                    doWeHaveToCleanUpOverviewTabFTPADoc(deletedFtpaDecisionDocIds, getIdFromDocUrl(idValue.getValue().getDocument().get().getDocumentUrl()))
            );
            asylumCase.write(documentType, documentWithDescriptionList);
        });
    }

    private boolean doWeHaveToCleanUpOverviewTabDoc(List<String> deletedFinalDecisionAndReasonsDocIds,
                                                    Document currentFinalDecisionAndReasonPdf) {
        String currentFinalDecisionAndReasonPdfId = getIdFromDocUrl(currentFinalDecisionAndReasonPdf.getDocumentUrl());
        return deletedFinalDecisionAndReasonsDocIds.contains(currentFinalDecisionAndReasonPdfId);
    }

    private boolean doWeHaveToCleanUpOverviewTabFTPADoc(List<String> deletedFinalDecisionAndReasonsDocIds,
                                                        String currentFTPADecisionAndReasonDocumentId) {
        return deletedFinalDecisionAndReasonsDocIds.contains(currentFTPADecisionAndReasonDocumentId);
    }

    private List<String> getDeletedDocIds(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        List<String> updatedAndDeletedDocIdsForGivenField = docsAuditService.getUpdatedAndDeletedDocIdsForGivenField(
            asylumCase, asylumCaseBefore, FINAL_DECISION_AND_REASONS_DOCUMENTS);
        Optional<List<IdValue<DocumentWithMetadata>>> optionalFinalDecisionAndReasonDocuments = asylumCase.read(
            FINAL_DECISION_AND_REASONS_DOCUMENTS);
        List<String> finalDecisionAndReasonDocIds = new ArrayList<>();
        if (optionalFinalDecisionAndReasonDocuments.isPresent()) {
            finalDecisionAndReasonDocIds = getFinalDecisionAndReasonDocIds(optionalFinalDecisionAndReasonDocuments.get());
        }
        updatedAndDeletedDocIdsForGivenField.removeAll(finalDecisionAndReasonDocIds);
        return updatedAndDeletedDocIdsForGivenField;
    }

    private List<String> getFinalDecisionAndReasonDocIds(
        List<IdValue<DocumentWithMetadata>> finalDecisionAndReasonDocuments) {
        return finalDecisionAndReasonDocuments.stream()
            .map(idValue -> getIdFromDocUrl(idValue.getValue().getDocument().getDocumentUrl()))
            .toList();
    }

    private List<String> getDeletedFtpaDocIds(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        List<String> updatedAndDeletedDocIdsForGivenField = new ArrayList<>();

        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIdsForGivenField, asylumCase, asylumCaseBefore, ALL_FTPA_APPELLANT_DECISION_DOCS);
        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIdsForGivenField, asylumCase, asylumCaseBefore, ALL_FTPA_RESPONDENT_DECISION_DOCS);
        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIdsForGivenField, asylumCase, asylumCaseBefore, FTPA_APPELLANT_DOCUMENTS);
        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIdsForGivenField, asylumCase, asylumCaseBefore, FTPA_RESPONDENT_DOCUMENTS);

        removeFromUpdatedAndDeletedDocIds(updatedAndDeletedDocIdsForGivenField, asylumCase.read(ALL_FTPA_APPELLANT_DECISION_DOCS));
        removeFromUpdatedAndDeletedDocIds(updatedAndDeletedDocIdsForGivenField, asylumCase.read(ALL_FTPA_RESPONDENT_DECISION_DOCS));
        removeFromUpdatedAndDeletedDocIds(updatedAndDeletedDocIdsForGivenField, asylumCase.read(FTPA_APPELLANT_DOCUMENTS));
        removeFromUpdatedAndDeletedDocIds(updatedAndDeletedDocIdsForGivenField, asylumCase.read(FTPA_RESPONDENT_DOCUMENTS));

        return updatedAndDeletedDocIdsForGivenField;
    }

    private void updateFtpaDecision(AsylumCase asylumCase, AsylumCaseFieldDefinition documentList, AsylumCaseFieldDefinition document) {
        Optional<List<IdValue<DocumentWithMetadata>>> optionalFtpaDecisionDocs = asylumCase.read(documentList);
        optionalFtpaDecisionDocs.ifPresent(ftpaDecisionDocs -> {
            ftpaDecisionDocs.stream()
                .filter(idValue -> idValue.getValue().getTag().equals(DocumentTag.FTPA_DECISION_AND_REASONS))
                .map(idValue -> new IdValue<>(
                    idValue.getId(),
                    new DocumentWithDescription(idValue.getValue().getDocument(), idValue.getValue().getDescription())
                ))
                .forEach(ftpaDecisionDocument -> asylumCase.write(document, Collections.singletonList(ftpaDecisionDocument)));
        });

    }

    private void addToUpdatedAndDeletedDocIds(List<String> list, AsylumCase asylumCase, AsylumCase asylumCaseBefore, AsylumCaseFieldDefinition documentType) {
        list.addAll(docsAuditService.getUpdatedAndDeletedDocIdsForGivenField(asylumCase, asylumCaseBefore, documentType));
    }

    private void removeFromUpdatedAndDeletedDocIds(List<String> updatedAndDeletedDocIds, Optional<List<IdValue<DocumentWithMetadata>>> optionalFtpaDocuments) {
        optionalFtpaDocuments.ifPresent(ftpaDocuments -> {
            List<String> ftpaDocIds = getFtpaDocIds(ftpaDocuments);
            updatedAndDeletedDocIds.removeAll(ftpaDocIds);
        });
    }

    private List<String> getFtpaDocIds(List<IdValue<DocumentWithMetadata>> ftpaDocuments) {
        return ftpaDocuments.stream()
                .map(idValue -> getIdFromDocUrl(idValue.getValue().getDocument().getDocumentUrl()))
                .toList();
    }
}
