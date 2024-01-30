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
        List<String> deletedFtpaDecisionDocIds = getDeletedFtpaDocIds(asylumCase, asylumCaseBefore);

        Document currentFinalDecisionAndReasonPdf = asylumCase.read(FINAL_DECISION_AND_REASONS_PDF, Document.class)
                .orElse(null);
        if (currentFinalDecisionAndReasonPdf != null
                && doWeHaveToCleanUpOverviewTabDoc(deletedFinalDecisionAndReasonsDocIds, currentFinalDecisionAndReasonPdf)) {
            asylumCase.clear(FINAL_DECISION_AND_REASONS_PDF);
        }

        Optional<List<IdValue<DocumentWithDescription>>> currentFTPAAppellantDecisionAndReasonDocuments = asylumCase.read(FTPA_APPELLANT_DECISION_DOCUMENT);
        currentFTPAAppellantDecisionAndReasonDocuments.ifPresent(documentWithDescriptionList -> {
            documentWithDescriptionList.removeIf(idValue ->
                    doWeHaveToCleanUpOverviewTabFTPADoc(deletedFtpaDecisionDocIds, getIdFromDocUrl(idValue.getValue().getDocument().get().getDocumentUrl()))
            );
            asylumCase.write(FTPA_APPELLANT_DECISION_DOCUMENT, documentWithDescriptionList);
        });

        Optional<List<IdValue<DocumentWithDescription>>> currentFTPARespondentDecisionAndReasonDocuments = asylumCase.read(FTPA_RESPONDENT_DECISION_DOCUMENT);
        currentFTPARespondentDecisionAndReasonDocuments.ifPresent(documentWithDescriptionList -> {
            documentWithDescriptionList.removeIf(idValue ->
                    doWeHaveToCleanUpOverviewTabFTPADoc(deletedFtpaDecisionDocIds, getIdFromDocUrl(idValue.getValue().getDocument().get().getDocumentUrl()))
            );
            asylumCase.write(FTPA_RESPONDENT_DECISION_DOCUMENT, documentWithDescriptionList);
        });

        Optional<List<IdValue<DocumentWithDescription>>> currentFTPAAppellantGroundsDocuments = asylumCase.read(FTPA_APPELLANT_GROUNDS_DOCUMENTS);
        currentFTPAAppellantGroundsDocuments.ifPresent(documentWithDescriptionList -> {
            documentWithDescriptionList.removeIf(idValue ->
                    doWeHaveToCleanUpOverviewTabFTPADoc(deletedFtpaDecisionDocIds, getIdFromDocUrl(idValue.getValue().getDocument().get().getDocumentUrl()))
            );
            asylumCase.write(FTPA_APPELLANT_GROUNDS_DOCUMENTS, documentWithDescriptionList);
        });

        Optional<List<IdValue<DocumentWithDescription>>> currentFTPARespondentGroundsDocuments = asylumCase.read(FTPA_RESPONDENT_GROUNDS_DOCUMENTS);
        currentFTPARespondentGroundsDocuments.ifPresent(documentWithDescriptionList -> {
            documentWithDescriptionList.removeIf(idValue ->
                    doWeHaveToCleanUpOverviewTabFTPADoc(deletedFtpaDecisionDocIds, getIdFromDocUrl(idValue.getValue().getDocument().get().getDocumentUrl()))
            );
            asylumCase.write(FTPA_RESPONDENT_GROUNDS_DOCUMENTS, documentWithDescriptionList);
        });

        Optional<List<IdValue<DocumentWithDescription>>> currentFTPAAppellantEvidenceDocuments = asylumCase.read(FTPA_APPELLANT_EVIDENCE_DOCUMENTS);
        currentFTPAAppellantEvidenceDocuments.ifPresent(documentWithDescriptionList -> {
            documentWithDescriptionList.removeIf(idValue ->
                    doWeHaveToCleanUpOverviewTabFTPADoc(deletedFtpaDecisionDocIds, getIdFromDocUrl(idValue.getValue().getDocument().get().getDocumentUrl()))
            );
            asylumCase.write(FTPA_APPELLANT_EVIDENCE_DOCUMENTS, documentWithDescriptionList);
        });

        Optional<List<IdValue<DocumentWithDescription>>> currentFTPARespondentEvidenceDocuments = asylumCase.read(FTPA_RESPONDENT_EVIDENCE_DOCUMENTS);
        currentFTPARespondentEvidenceDocuments.ifPresent(documentWithDescriptionList -> {
            documentWithDescriptionList.removeIf(idValue ->
                    doWeHaveToCleanUpOverviewTabFTPADoc(deletedFtpaDecisionDocIds, getIdFromDocUrl(idValue.getValue().getDocument().get().getDocumentUrl()))
            );
            asylumCase.write(FTPA_RESPONDENT_EVIDENCE_DOCUMENTS, documentWithDescriptionList);
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
        List<String> updatedAndDeletedDocIdsForGivenField = docsAuditService.getUpdatedAndDeletedDocIdsForGivenField(
                asylumCase, asylumCaseBefore, ALL_FTPA_APPELLANT_DECISION_DOCS);
        updatedAndDeletedDocIdsForGivenField.addAll(docsAuditService.getUpdatedAndDeletedDocIdsForGivenField(
                asylumCase, asylumCaseBefore, ALL_FTPA_RESPONDENT_DECISION_DOCS));
        updatedAndDeletedDocIdsForGivenField.addAll(docsAuditService.getUpdatedAndDeletedDocIdsForGivenField(
                asylumCase, asylumCaseBefore, FTPA_APPELLANT_DOCUMENTS));
        updatedAndDeletedDocIdsForGivenField.addAll(docsAuditService.getUpdatedAndDeletedDocIdsForGivenField(
                asylumCase, asylumCaseBefore, FTPA_RESPONDENT_DOCUMENTS));
        Optional<List<IdValue<DocumentWithMetadata>>> optionalFtpaAppellantDecisionDocument = asylumCase.read(
                ALL_FTPA_APPELLANT_DECISION_DOCS);
        List<String> ftpaAppellantDecisionDocIds = new ArrayList<>();
        if (optionalFtpaAppellantDecisionDocument.isPresent()) {
            ftpaAppellantDecisionDocIds = getFtpaDocIds(optionalFtpaAppellantDecisionDocument.get());
        }
        Optional<List<IdValue<DocumentWithMetadata>>> optionalFtpaRespondentDecisionDocument = asylumCase.read(
                ALL_FTPA_RESPONDENT_DECISION_DOCS);
        List<String> ftpaRespondentDecisionDocIds = new ArrayList<>();
        if (optionalFtpaRespondentDecisionDocument.isPresent()) {
            ftpaRespondentDecisionDocIds = getFtpaDocIds(optionalFtpaRespondentDecisionDocument.get());
        }
        Optional<List<IdValue<DocumentWithMetadata>>> optionalFtpaAppellantDocuments = asylumCase.read(
                FTPA_APPELLANT_DOCUMENTS);
        List<String> ftpaAppellantDocIds = new ArrayList<>();
        if (optionalFtpaAppellantDocuments.isPresent()) {
            ftpaAppellantDocIds = getFtpaDocIds(optionalFtpaAppellantDocuments.get());
        }
        Optional<List<IdValue<DocumentWithMetadata>>> optionalFtpaRespondentDocuments = asylumCase.read(
                FTPA_RESPONDENT_DOCUMENTS);
        List<String> ftpaRespondentDocIds = new ArrayList<>();
        if (optionalFtpaRespondentDocuments.isPresent()) {
            ftpaRespondentDocIds = getFtpaDocIds(optionalFtpaRespondentDocuments.get());
        }
        updatedAndDeletedDocIdsForGivenField.removeAll(ftpaAppellantDecisionDocIds);
        updatedAndDeletedDocIdsForGivenField.removeAll(ftpaRespondentDecisionDocIds);
        updatedAndDeletedDocIdsForGivenField.removeAll(ftpaAppellantDocIds);
        updatedAndDeletedDocIdsForGivenField.removeAll(ftpaRespondentDocIds);
        return updatedAndDeletedDocIdsForGivenField;
    }

    private List<String> getFtpaDocIds(
            List<IdValue<DocumentWithMetadata>> ftpaDocuments) {
        return ftpaDocuments.stream()
                .map(idValue -> getIdFromDocUrl(idValue.getValue().getDocument().getDocumentUrl()))
                .toList();
    }
}
