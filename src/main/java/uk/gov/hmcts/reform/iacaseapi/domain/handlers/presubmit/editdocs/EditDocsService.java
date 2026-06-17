package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.EditDocsAuditService.getIdFromDocUrl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.EditDocsAuditService;

@Slf4j
@Service
public class EditDocsService {

    private final EditDocsAuditService docsAuditService;

    public EditDocsService(EditDocsAuditService docsAuditService) {
        this.docsAuditService = docsAuditService;
    }

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
        List<String> deletedFtpaApplicationDocIds = getDeletedFtpaApplicationDocIds(asylumCase, asylumCaseBefore);
        cleanUpFtpaApplicationDocuments(asylumCase, deletedFtpaApplicationDocIds);
    }

    public void cleanUpAppealTabDocs(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        List<String> deletedLRDocIds = getDeletedAppealDocIds(asylumCase, asylumCaseBefore);
        cleanUpAppealDocumentList(asylumCase, REASONS_FOR_APPEAL_DOCUMENTS, deletedLRDocIds);
    }

    private void cleanUpDocumentList(
            AsylumCase asylumCase,
            AsylumCaseFieldDefinition documentType,
            List<String> deletedDocIds
    ) {
        asylumCase.read(documentType, Document.class)
                .filter(document -> isDeletedDocument(document, deletedDocIds))
                .ifPresent(document -> asylumCase.clear(documentType));
    }

    private void cleanUpFTPADocumentList(
            AsylumCase asylumCase,
            AsylumCaseFieldDefinition documentType,
            List<String> deletedDocIds
    ) {
        Optional<List<IdValue<DocumentWithDescription>>> currentDocuments =
                asylumCase.read(documentType);

        currentDocuments.ifPresent(documents -> {
            documents.removeIf(idValue ->
                    isDeletedDocument(
                            idValue.getValue().getDocument().orElse(null),
                            deletedDocIds
                    )
            );
            asylumCase.write(documentType, documents);
        });
    }

    private void cleanUpAppealDocumentList(
            AsylumCase asylumCase,
            AsylumCaseFieldDefinition documentType,
            List<String> deletedDocIds
    ) {
        Optional<List<IdValue<DocumentWithMetadata>>> currentDocuments =
                asylumCase.read(documentType);

        currentDocuments.ifPresent(documents -> {
            documents.removeIf(idValue ->
                    isDeletedDocument(
                            idValue.getValue().getDocument(),
                            deletedDocIds
                    )
            );

            asylumCase.write(documentType, documents);

            if (documentType.equals(REASONS_FOR_APPEAL_DOCUMENTS) && documents.isEmpty()) {
                asylumCase.clear(REASONS_FOR_APPEAL_DECISION);
                asylumCase.clear(REASONS_FOR_APPEAL_DATE_UPLOADED);
            }
        });
    }

    private void cleanUpFtpaApplicationDocuments(AsylumCase asylumCase, List<String> deletedDocIds) {
        Optional<List<IdValue<FtpaApplications>>> optionalFtpaList = asylumCase.read(FTPA_LIST);
        optionalFtpaList.ifPresent(ftpaList -> {
            ftpaList.removeIf(ftpaApplicationIdValue -> {
                FtpaApplications ftpaApplication = ftpaApplicationIdValue.getValue();
                removeDeletedDocuments(ftpaApplication.getFtpaOutOfTimeDocuments(), deletedDocIds);
                removeDeletedDocuments(ftpaApplication.getFtpaGroundsDocuments(), deletedDocIds);
                removeDeletedDocuments(ftpaApplication.getFtpaEvidenceDocuments(), deletedDocIds);
                return isEmpty(ftpaApplication.getFtpaOutOfTimeDocuments())
                        && isEmpty(ftpaApplication.getFtpaGroundsDocuments())
                        && isEmpty(ftpaApplication.getFtpaEvidenceDocuments());
            });
            asylumCase.write(FTPA_LIST, ftpaList);
        });
    }

    private boolean isDeletedDocument(Document document, List<String> deletedDocIds) {
        return document != null
                && deletedDocIds.contains(getIdFromDocUrl(document.getDocumentUrl()));
    }

    private void removeDeletedDocuments(
            List<IdValue<DocumentWithDescription>> documents,
            List<String> deletedDocIds
    ) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        documents.removeIf(idValue ->
                isDeletedDocument(
                        idValue.getValue().getDocument().orElse(null),
                        deletedDocIds
                )
        );
    }

    private List<String> getDeletedAppealDocIds(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        List<String> updatedAndDeletedDocIds =
                docsAuditService.getUpdatedAndDeletedDocIdsForGivenField(
                        asylumCase,
                        asylumCaseBefore,
                        LEGAL_REPRESENTATIVE_DOCUMENTS
                );
        removeFromUpdatedAndDeletedDocIds(
                updatedAndDeletedDocIds,
                asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)
        );
        return updatedAndDeletedDocIds;
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

    private void updateFtpaNonDecisionDocDescriptions(AsylumCase asylumCase, AsylumCase asylumCaseBefore, List<String> deletedFtpaDocIds) {
        List<String> updatedAndDeletedDocIdsForGivenField = new ArrayList<>();

        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIdsForGivenField, asylumCase, asylumCaseBefore, FTPA_APPELLANT_DOCUMENTS);
        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIdsForGivenField, asylumCase, asylumCaseBefore, FTPA_RESPONDENT_DOCUMENTS);
        updateFtpaDocDescriptions(asylumCase, updatedAndDeletedDocIdsForGivenField, deletedFtpaDocIds, FTPA_APPELLANT_DOCUMENTS, FTPA_APPELLANT_GROUNDS_DOCUMENTS);
        updateFtpaDocDescriptions(asylumCase, updatedAndDeletedDocIdsForGivenField, deletedFtpaDocIds, FTPA_APPELLANT_DOCUMENTS, FTPA_APPELLANT_EVIDENCE_DOCUMENTS);
        updateFtpaDocDescriptions(asylumCase, updatedAndDeletedDocIdsForGivenField, deletedFtpaDocIds, FTPA_RESPONDENT_DOCUMENTS, FTPA_RESPONDENT_GROUNDS_DOCUMENTS);
        updateFtpaDocDescriptions(asylumCase, updatedAndDeletedDocIdsForGivenField, deletedFtpaDocIds, FTPA_RESPONDENT_DOCUMENTS, FTPA_RESPONDENT_EVIDENCE_DOCUMENTS);
    }

    private List<String> getDeletedDocIds(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        List<String> updatedAndDeletedDocIds =
                docsAuditService.getUpdatedAndDeletedDocIdsForGivenField(
                        asylumCase,
                        asylumCaseBefore,
                        FINAL_DECISION_AND_REASONS_DOCUMENTS
                );
        removeFromUpdatedAndDeletedDocIds(
                updatedAndDeletedDocIds,
                asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENTS)
        );
        return updatedAndDeletedDocIds;
    }

    private List<String> getDeletedFtpaApplicationDocIds(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        List<String> updatedAndDeletedDocIds = new ArrayList<>();
        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIds, asylumCase, asylumCaseBefore, FTPA_APPELLANT_DOCUMENTS);
        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIds, asylumCase, asylumCaseBefore, FTPA_RESPONDENT_DOCUMENTS);
        removeFromUpdatedAndDeletedDocIds(updatedAndDeletedDocIds, asylumCase.read(FTPA_APPELLANT_DOCUMENTS));
        removeFromUpdatedAndDeletedDocIds(updatedAndDeletedDocIds, asylumCase.read(FTPA_RESPONDENT_DOCUMENTS));
        return updatedAndDeletedDocIds;
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

    private void updateFtpaDocDescriptions(AsylumCase asylumCase, List<String> updatedAndDeletedDocIds, List<String> deletedFtpaDocIds, AsylumCaseFieldDefinition docTabDocuments, AsylumCaseFieldDefinition ftpaTabDocuments) {
        Optional<List<IdValue<DocumentWithMetadata>>> optionalFtpaDocuments = asylumCase.read(docTabDocuments);
        Optional<List<IdValue<DocumentWithDescription>>> optionalFtpaDocumentsDescription = asylumCase.read(ftpaTabDocuments);
        optionalFtpaDocuments.ifPresent(ftpaDocuments ->
                optionalFtpaDocumentsDescription.ifPresent(ftpaDocumentsDescription -> {
                    updatedAndDeletedDocIds.forEach(updatedDocId -> {
                        if (deletedFtpaDocIds.contains(updatedDocId)) {
                            return;
                        }
                        Optional<IdValue<DocumentWithDescription>> optionalOldIdValue =
                                ftpaDocumentsDescription.stream()
                                        .filter(document -> {
                                            Document doc = document.getValue()
                                                    .getDocument()
                                                    .orElse(null);

                                            return doc != null
                                                    && getIdFromDocUrl(doc.getDocumentUrl())
                                                    .equals(updatedDocId);
                                        })
                                        .findFirst();

                        Optional<IdValue<DocumentWithMetadata>> optionalUpdatedDoc =
                                ftpaDocuments.stream()
                                        .filter(document ->
                                                getIdFromDocUrl(
                                                        document.getValue()
                                                                .getDocument()
                                                                .getDocumentUrl()
                                                ).equals(updatedDocId)
                                        )
                                        .findFirst();

                        if (optionalOldIdValue.isEmpty() || optionalUpdatedDoc.isEmpty()) {
                            return;
                        }

                        IdValue<DocumentWithDescription> oldIdValue = optionalOldIdValue.get();
                        String newDescription = optionalUpdatedDoc.get().getValue().getDescription();

                        oldIdValue.getValue().setDescription(Optional.ofNullable(newDescription));
                    });
                    asylumCase.write(ftpaTabDocuments, ftpaDocumentsDescription);
                })
        );
    }

    private void addToUpdatedAndDeletedDocIds(List<String> list, AsylumCase asylumCase, AsylumCase asylumCaseBefore, AsylumCaseFieldDefinition documentType) {
        list.addAll(
                docsAuditService.getUpdatedAndDeletedDocIdsForGivenField(
                        asylumCase,
                        asylumCaseBefore,
                        documentType
                )
        );
    }

    private void removeFromUpdatedAndDeletedDocIds(List<String> updatedAndDeletedDocIds, Optional<List<IdValue<DocumentWithMetadata>>> optionalDocuments) {
        optionalDocuments.ifPresent(documents -> {
            List<String> currentDocIds =
                    documents.stream()
                            .map(idValue ->
                                    getIdFromDocUrl(
                                            idValue.getValue()
                                                    .getDocument()
                                                    .getDocumentUrl()
                                    )
                            )
                            .toList();
            updatedAndDeletedDocIds.removeAll(currentDocIds);
        });
    }
}
