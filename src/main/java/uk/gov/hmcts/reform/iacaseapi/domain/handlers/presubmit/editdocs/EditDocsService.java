package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.EditDocsAuditService.getIdFromDocUrl;

import java.util.ArrayList;
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
        List<String> deletedFtpaDocIds = getDeletedFtpaDocIds(asylumCase, asylumCaseBefore);
        updateFtpaNonDecisionDocDescriptions(asylumCase, asylumCaseBefore, deletedFtpaDocIds);

        cleanUpDocumentList(asylumCase, FINAL_DECISION_AND_REASONS_PDF, deletedFinalDecisionAndReasonsDocIds);

        cleanUpFTPADocumentList(asylumCase, FTPA_APPELLANT_DECISION_DOCUMENT, deletedFtpaDocIds);
        cleanUpFTPADocumentList(asylumCase, FTPA_RESPONDENT_DECISION_DOCUMENT, deletedFtpaDocIds);
        cleanUpFTPADocumentList(asylumCase, FTPA_APPELLANT_GROUNDS_DOCUMENTS, deletedFtpaDocIds);
        cleanUpFTPADocumentList(asylumCase, FTPA_RESPONDENT_GROUNDS_DOCUMENTS, deletedFtpaDocIds);
        cleanUpFTPADocumentList(asylumCase, FTPA_APPELLANT_EVIDENCE_DOCUMENTS, deletedFtpaDocIds);
        cleanUpFTPADocumentList(asylumCase, FTPA_RESPONDENT_EVIDENCE_DOCUMENTS, deletedFtpaDocIds);
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

    private List<String> updateFtpaNonDecisionDocDescriptions(AsylumCase asylumCase, AsylumCase asylumCaseBefore, List<String> deletedFtpaDocIds) {
        List<String> updatedAndDeletedDocIdsForGivenField = new ArrayList<>();

        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIdsForGivenField, asylumCase, asylumCaseBefore, FTPA_APPELLANT_DOCUMENTS);
        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIdsForGivenField, asylumCase, asylumCaseBefore, FTPA_RESPONDENT_DOCUMENTS);
        updateFtpaDocDescriptions(asylumCase, updatedAndDeletedDocIdsForGivenField, deletedFtpaDocIds, FTPA_APPELLANT_DOCUMENTS, FTPA_APPELLANT_GROUNDS_DOCUMENTS);
        updateFtpaDocDescriptions(asylumCase, updatedAndDeletedDocIdsForGivenField, deletedFtpaDocIds, FTPA_APPELLANT_DOCUMENTS, FTPA_APPELLANT_EVIDENCE_DOCUMENTS);
        updateFtpaDocDescriptions(asylumCase, updatedAndDeletedDocIdsForGivenField, deletedFtpaDocIds, FTPA_RESPONDENT_DOCUMENTS, FTPA_RESPONDENT_GROUNDS_DOCUMENTS);
        updateFtpaDocDescriptions(asylumCase, updatedAndDeletedDocIdsForGivenField, deletedFtpaDocIds, FTPA_RESPONDENT_DOCUMENTS, FTPA_RESPONDENT_EVIDENCE_DOCUMENTS);

        return updatedAndDeletedDocIdsForGivenField;
    }

    private void updateFtpaDecision(AsylumCase asylumCase, AsylumCaseFieldDefinition documentList, AsylumCaseFieldDefinition document) {
        Optional<List<IdValue<DocumentWithMetadata>>> optionalFtpaDecisionDocs = asylumCase.read(documentList);
        optionalFtpaDecisionDocs.ifPresent(ftpaDecisionDocs -> {
            List<IdValue<DocumentWithDescription>> newDecisionDocuments = ftpaDecisionDocs.stream()
                .filter(idValue -> idValue.getValue().getTag().equals(DocumentTag.FTPA_DECISION_AND_REASONS))
                .map(idValue -> new IdValue<>(
                    idValue.getId(),
                    new DocumentWithDescription(idValue.getValue().getDocument(), idValue.getValue().getDescription())
                ))
                .toList();
            asylumCase.write(document, newDecisionDocuments);
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

    private void updateFtpaDocDescriptions(AsylumCase asylumCase, List<String> updatedAndDeletedDocIds, List<String> deletedFtpaDocIds, AsylumCaseFieldDefinition docTabDocuments, AsylumCaseFieldDefinition ftpaTabDocuments) {
        Optional<List<IdValue<DocumentWithMetadata>>> optionalFtpaDocuments = asylumCase.read(docTabDocuments);
        Optional<List<IdValue<DocumentWithDescription>>> optionalFtpaDocumentsDescription = asylumCase.read(ftpaTabDocuments);
        optionalFtpaDocuments.ifPresent(ftpaDocuments -> {
            optionalFtpaDocumentsDescription.ifPresent(ftpaDocumentsDescription -> {
                updatedAndDeletedDocIds.forEach((updatedDocId) -> {
                    List<String> currentFtpaDocDescriptionIds = getFtpaDocDescriptionIds(ftpaDocumentsDescription);
                    if (currentFtpaDocDescriptionIds.contains(updatedDocId) && !deletedFtpaDocIds.contains(updatedDocId)) {
                        IdValue<DocumentWithDescription> oldIdValue = ftpaDocumentsDescription.stream().filter((document) -> getIdFromDocUrl(document.getValue().getDocument().get().getDocumentUrl()).equals(updatedDocId))
                            .findFirst().get();
                        String newDescription = ftpaDocuments.stream().filter((document) -> getIdFromDocUrl(document.getValue().getDocument().getDocumentUrl()).equals(updatedDocId))
                            .findFirst().get().getValue().getDescription();
                        IdValue<DocumentWithDescription> newIdValue = new IdValue<>(oldIdValue.getId(), new DocumentWithDescription(oldIdValue.getValue().getDocument().get(), newDescription));
                        ftpaDocumentsDescription.remove(oldIdValue);
                        ftpaDocumentsDescription.add(newIdValue);
                    }
                });
                asylumCase.write(ftpaTabDocuments, ftpaDocumentsDescription);
            });
        });
    }

    private List<String> getFtpaDocIds(List<IdValue<DocumentWithMetadata>> ftpaDocuments) {
        return ftpaDocuments.stream()
                .map(idValue -> getIdFromDocUrl(idValue.getValue().getDocument().getDocumentUrl()))
                .toList();
    }

    private List<String> getFtpaDocDescriptionIds(List<IdValue<DocumentWithDescription>> ftpaDocuments) {
        return ftpaDocuments.stream()
            .map(idValue -> getIdFromDocUrl(idValue.getValue().getDocument().get().getDocumentUrl()))
            .toList();
    }
}
