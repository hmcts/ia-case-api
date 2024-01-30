package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.EditDocsAuditService.getIdFromDocUrl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
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
        List<String> editedFtpaDocIds = getEditedFtpaDocIds(asylumCase, asylumCaseBefore);
        List<String> deletedFtpaDocIds = getDeletedFtpaDocIds(asylumCase, asylumCaseBefore);
        cleanUpDocumentList(asylumCase, FINAL_DECISION_AND_REASONS_PDF, deletedFinalDecisionAndReasonsDocIds);

        cleanUpFTPADocumentList(asylumCase, FTPA_APPELLANT_DECISION_DOCUMENT, ALL_FTPA_APPELLANT_DECISION_DOCS, deletedFtpaDocIds, editedFtpaDocIds);
        cleanUpFTPADocumentList(asylumCase, FTPA_RESPONDENT_DECISION_DOCUMENT, ALL_FTPA_RESPONDENT_DECISION_DOCS, deletedFtpaDocIds, editedFtpaDocIds);
        cleanUpFTPADocumentList(asylumCase, FTPA_APPELLANT_GROUNDS_DOCUMENTS, FTPA_APPELLANT_DOCUMENTS, deletedFtpaDocIds, editedFtpaDocIds);
        cleanUpFTPADocumentList(asylumCase, FTPA_RESPONDENT_GROUNDS_DOCUMENTS, FTPA_RESPONDENT_DOCUMENTS, deletedFtpaDocIds, editedFtpaDocIds);
        cleanUpFTPADocumentList(asylumCase, FTPA_APPELLANT_EVIDENCE_DOCUMENTS, FTPA_APPELLANT_DOCUMENTS, deletedFtpaDocIds, editedFtpaDocIds);
        cleanUpFTPADocumentList(asylumCase, FTPA_RESPONDENT_EVIDENCE_DOCUMENTS, FTPA_RESPONDENT_DOCUMENTS, deletedFtpaDocIds, editedFtpaDocIds);
    }

    private void cleanUpDocumentList(AsylumCase asylumCase, AsylumCaseFieldDefinition documentType, List<String> deletedDocIds) {
        Document currentDocument = asylumCase.read(documentType, Document.class).orElse(null);
        if (currentDocument != null && doWeHaveToCleanUpOverviewTabDoc(deletedDocIds, currentDocument)) {
            asylumCase.clear(documentType);
        }
    }

    private void cleanUpFTPADocumentList(AsylumCase asylumCase, AsylumCaseFieldDefinition documentType, AsylumCaseFieldDefinition documentTypeList, List<String> deletedFtpaDecisionDocIds, List<String> editedFtpaDecisionDocIds) {
        Optional<List<IdValue<DocumentWithDescription>>> currentDocuments = asylumCase.read(documentType);
        Optional<List<IdValue<DocumentWithDescription>>> currentDocumentsList = asylumCase.read(documentTypeList);
        currentDocuments.ifPresent(documentWithDescriptionList -> {
            documentWithDescriptionList.removeIf(idValue ->
                doWeHaveToCleanUpOverviewTabFTPADoc(deletedFtpaDecisionDocIds, getIdFromDocUrl(idValue.getValue().getDocument().get().getDocumentUrl()))
            );
            log.info("Size of edited list = " + editedFtpaDecisionDocIds.size());
            for (String id : editedFtpaDecisionDocIds) {
                log.info(id);
            }
            for (IdValue<DocumentWithDescription> idValue : documentWithDescriptionList) {
                log.info("1 " + idValue.getValue().getDocument().get().getDocumentFilename());
                log.info(documentType.value());
                log.info(getIdFromDocUrl(idValue.getValue().getDocument().get().getDocumentUrl()));
                if (editedFtpaDecisionDocIds.contains(getIdFromDocUrl(idValue.getValue().getDocument().get().getDocumentUrl()))) {
                    log.info("2 " + idValue.getValue().getDocument().get().getDocumentFilename());
                    for (IdValue<DocumentWithDescription> idValue2 : currentDocumentsList.get()) {
                        log.info("3 " + idValue2.getValue().getDocument().get().getDocumentFilename());
                        if (getIdFromDocUrl(idValue.getValue().getDocument().get().getDocumentUrl()).equals(getIdFromDocUrl(idValue2.getValue().getDocument().get().getDocumentUrl()))) {
                            log.info("4 " + idValue2.getValue().getDocument().get().getDocumentFilename());
                            idValue = idValue2;
                            log.info("5 " + idValue.getValue().getDocument().get().getDocumentFilename());
                            log.info("6 " + idValue2.getValue().getDocument().get().getDocumentFilename());
                        }
                    }
                }
            }
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

    private List<String> getEditedFtpaDocIds(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        List<String> updatedAndDeletedDocIdsForGivenField = new ArrayList<>();
        List<String> updatedDocIdsForGivenField = new ArrayList<>();
        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIdsForGivenField, asylumCase, asylumCaseBefore, ALL_FTPA_APPELLANT_DECISION_DOCS);
        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIdsForGivenField, asylumCase, asylumCaseBefore, ALL_FTPA_RESPONDENT_DECISION_DOCS);
        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIdsForGivenField, asylumCase, asylumCaseBefore, FTPA_APPELLANT_DOCUMENTS);
        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIdsForGivenField, asylumCase, asylumCaseBefore, FTPA_RESPONDENT_DOCUMENTS);

        addToUpdatedDocIds(updatedAndDeletedDocIdsForGivenField, updatedDocIdsForGivenField, asylumCase.read(ALL_FTPA_APPELLANT_DECISION_DOCS));
        addToUpdatedDocIds(updatedAndDeletedDocIdsForGivenField, updatedDocIdsForGivenField, asylumCase.read(ALL_FTPA_RESPONDENT_DECISION_DOCS));
        addToUpdatedDocIds(updatedAndDeletedDocIdsForGivenField, updatedDocIdsForGivenField, asylumCase.read(FTPA_APPELLANT_DOCUMENTS));
        addToUpdatedDocIds(updatedAndDeletedDocIdsForGivenField, updatedDocIdsForGivenField, asylumCase.read(FTPA_RESPONDENT_DOCUMENTS));

        return updatedDocIdsForGivenField;
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

    private void addToUpdatedDocIds(List<String> updatedAndDeletedDocIds, List<String> updatedDocIds, Optional<List<IdValue<DocumentWithMetadata>>> optionalFtpaDocuments) {
        optionalFtpaDocuments.ifPresent(ftpaDocuments -> {
            List<String> ftpaDocIds = getFtpaDocIds(ftpaDocuments);
            List<String> commonDocIds = updatedAndDeletedDocIds.stream()
                .filter(ftpaDocIds::contains)
                .toList();
            updatedDocIds.addAll(commonDocIds);
        });
    }

    private List<String> getFtpaDocIds(List<IdValue<DocumentWithMetadata>> ftpaDocuments) {
        return ftpaDocuments.stream()
            .map(idValue -> getIdFromDocUrl(idValue.getValue().getDocument().getDocumentUrl()))
            .toList();
    }
}
