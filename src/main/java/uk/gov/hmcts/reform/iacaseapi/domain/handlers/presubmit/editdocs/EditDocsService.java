package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.EditDocsAuditService.getIdFromDocUrl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    private static final List<AsylumCaseFieldDefinition> FTPA_DOCUMENT_FIELDS =
            List.of(
                    FTPA_APPELLANT_DECISION_DOCUMENT,
                    FTPA_RESPONDENT_DECISION_DOCUMENT,
                    FTPA_APPELLANT_GROUNDS_DOCUMENTS,
                    FTPA_RESPONDENT_GROUNDS_DOCUMENTS,
                    FTPA_APPELLANT_EVIDENCE_DOCUMENTS,
                    FTPA_RESPONDENT_EVIDENCE_DOCUMENTS
            );

    public EditDocsService(EditDocsAuditService docsAuditService) {
        this.docsAuditService = docsAuditService;
    }

    public void cleanUpOverviewTabDocs(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        List<String> deletedFinalDecisionAndReasonsDocIds = getDeletedDocIds(asylumCase, asylumCaseBefore);
        List<String> deletedFtpaDocIds = getDeletedFtpaDocIds(asylumCase, asylumCaseBefore);
        updateFtpaNonDecisionDocDescriptions(asylumCase, asylumCaseBefore, deletedFtpaDocIds);
        cleanUpDocumentList(asylumCase, FINAL_DECISION_AND_REASONS_PDF, deletedFinalDecisionAndReasonsDocIds);

        FTPA_DOCUMENT_FIELDS.forEach(field ->
                cleanUpFTPADocumentList(
                        asylumCase,
                        field,
                        deletedFtpaDocIds
                )
        );

        updateFtpaDecision(
                asylumCase,
                ALL_FTPA_APPELLANT_DECISION_DOCS,
                FTPA_APPELLANT_DECISION_DOCUMENT
        );

        updateFtpaDecision(
                asylumCase,
                ALL_FTPA_RESPONDENT_DECISION_DOCS,
                FTPA_RESPONDENT_DECISION_DOCUMENT
        );

        List<String> deletedFtpaApplicationDocIds =
                getDeletedFtpaApplicationDocIds(
                        asylumCase,
                        asylumCaseBefore
                );

        cleanUpFtpaApplicationDocuments(
                asylumCase,
                deletedFtpaApplicationDocIds
        );
    }

    public void cleanUpAppealTabDocs(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        List<String> deletedLRDocIds = getDeletedAppealDocIds(asylumCase, asylumCaseBefore);
        cleanUpAppealDocumentList(asylumCase, REASONS_FOR_APPEAL_DOCUMENTS, deletedLRDocIds);
    }

    private void cleanUpDocumentList(AsylumCase asylumCase, AsylumCaseFieldDefinition documentType, List<String> deletedDocIds
    ) {
        Document currentDocument = asylumCase.read(documentType, Document.class).orElse(null);
        if (currentDocument == null) {
            return;
        }

        String documentId =
                getIdFromDocUrl(currentDocument.getDocumentUrl());

        if (deletedDocIds.contains(documentId)) {
            asylumCase.clear(documentType);
        }
    }

    private void cleanUpFTPADocumentList(AsylumCase asylumCase, AsylumCaseFieldDefinition documentType, List<String> deletedDocIds
    ) {
        Set<String> deletedDocIdSet = new HashSet<>(deletedDocIds);

        Optional<List<IdValue<DocumentWithDescription>>> optionalDocuments = asylumCase.read(documentType);

        optionalDocuments.ifPresent(documents -> {

            documents.removeIf(idValue -> {

                Document document =
                        idValue.getValue()
                                .getDocument()
                                .orElse(null);

                if (document == null) {
                    return false;
                }

                String documentId =
                        getIdFromDocUrl(
                                document.getDocumentUrl()
                        );

                return deletedDocIdSet.contains(documentId);
            });

            asylumCase.write(documentType, documents);
        });
    }

    private void cleanUpAppealDocumentList(
            AsylumCase asylumCase,
            AsylumCaseFieldDefinition documentType,
            List<String> deletedDocIds
    ) {

        Set<String> deletedDocIdSet =
                new HashSet<>(deletedDocIds);

        Optional<List<IdValue<DocumentWithMetadata>>> currentDocuments =
                asylumCase.read(documentType);

        currentDocuments.ifPresent(documentList -> {

            documentList.removeIf(idValue ->
                    deletedDocIdSet.contains(
                            getIdFromDocUrl(
                                    idValue.getValue()
                                            .getDocument()
                                            .getDocumentUrl()
                            )
                    )
            );

            asylumCase.write(documentType, documentList);

            if (documentType.equals(REASONS_FOR_APPEAL_DOCUMENTS)
                    && documentList.isEmpty()) {

                asylumCase.clear(REASONS_FOR_APPEAL_DECISION);
                asylumCase.clear(REASONS_FOR_APPEAL_DATE_UPLOADED);
            }
        });
    }

    private void cleanUpFtpaApplicationDocuments(
            AsylumCase asylumCase,
            List<String> deletedDocIds
    ) {

        Set<String> deletedDocIdSet =
                new HashSet<>(deletedDocIds);

        Optional<List<IdValue<FtpaApplications>>> optionalFtpaList =
                asylumCase.read(FTPA_LIST);

        optionalFtpaList.ifPresent(ftpaList -> {

            ftpaList.removeIf(ftpaApplicationIdValue -> {

                FtpaApplications ftpaApplication =
                        ftpaApplicationIdValue.getValue();

                removeDeletedDocuments(
                        ftpaApplication.getFtpaOutOfTimeDocuments(),
                        deletedDocIdSet
                );

                removeDeletedDocuments(
                        ftpaApplication.getFtpaGroundsDocuments(),
                        deletedDocIdSet
                );

                removeDeletedDocuments(
                        ftpaApplication.getFtpaEvidenceDocuments(),
                        deletedDocIdSet
                );

                boolean noOutOfTime =
                        isEmpty(ftpaApplication.getFtpaOutOfTimeDocuments());

                boolean noGrounds =
                        isEmpty(ftpaApplication.getFtpaGroundsDocuments());

                boolean noEvidence =
                        isEmpty(ftpaApplication.getFtpaEvidenceDocuments());

                if (noOutOfTime && noGrounds && noEvidence) {
                    log.info("Removing FTPA application as all document lists are empty");
                    return true;
                }

                ftpaApplication.setFtpaApplicationDate(null);
                ftpaApplication.setFtpaApplicant(null);

                return false;
            });

            asylumCase.write(FTPA_LIST, ftpaList);
        });
    }

    private void removeDeletedDocuments(
            List<IdValue<DocumentWithDescription>> documents,
            Set<String> deletedDocIds
    ) {

        if (documents == null || documents.isEmpty()) {
            return;
        }

        documents.removeIf(idValue -> {

            Document document =
                    idValue.getValue()
                            .getDocument()
                            .orElse(null);

            if (document == null) {
                return false;
            }

            String documentId =
                    getIdFromDocUrl(document.getDocumentUrl());

            return deletedDocIds.contains(documentId);
        });
    }

    private List<String> getDeletedAppealDocIds(
            AsylumCase asylumCase,
            AsylumCase asylumCaseBefore
    ) {

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

    private List<String> getDeletedDocIds(
            AsylumCase asylumCase,
            AsylumCase asylumCaseBefore
    ) {

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

    private List<String> getDeletedFtpaDocIds(
            AsylumCase asylumCase,
            AsylumCase asylumCaseBefore
    ) {

        List<String> updatedAndDeletedDocIds =
                new ArrayList<>();

        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIds, asylumCase, asylumCaseBefore, ALL_FTPA_APPELLANT_DECISION_DOCS);

        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIds, asylumCase, asylumCaseBefore, ALL_FTPA_RESPONDENT_DECISION_DOCS);

        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIds, asylumCase, asylumCaseBefore, FTPA_APPELLANT_DOCUMENTS);

        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIds, asylumCase, asylumCaseBefore, FTPA_RESPONDENT_DOCUMENTS);

        removeFromUpdatedAndDeletedDocIds(updatedAndDeletedDocIds, asylumCase.read(ALL_FTPA_APPELLANT_DECISION_DOCS));

        removeFromUpdatedAndDeletedDocIds(updatedAndDeletedDocIds, asylumCase.read(ALL_FTPA_RESPONDENT_DECISION_DOCS));

        removeFromUpdatedAndDeletedDocIds(updatedAndDeletedDocIds, asylumCase.read(FTPA_APPELLANT_DOCUMENTS));

        removeFromUpdatedAndDeletedDocIds(updatedAndDeletedDocIds, asylumCase.read(FTPA_RESPONDENT_DOCUMENTS));

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
                    .filter(idValue ->
                                    idValue.getValue()
                                            .getTag()
                                            .equals(DocumentTag.FTPA_DECISION_AND_REASONS)
                        )
                        .map(idValue ->
                                new IdValue<>(
                                        idValue.getId(),
                                        new DocumentWithDescription(
                                                idValue.getValue().getDocument(),
                                                idValue.getValue().getDescription()
                                        )
                                )
                        )
                        .toList();
            asylumCase.write(document, newDecisionDocuments);
        });
    }

    private void updateFtpaNonDecisionDocDescriptions(AsylumCase asylumCase, AsylumCase asylumCaseBefore, List<String> deletedFtpaDocIds) {
        List<String> updatedAndDeletedDocIds = new ArrayList<>();
        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIds, asylumCase, asylumCaseBefore, FTPA_APPELLANT_DOCUMENTS);
        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIds, asylumCase, asylumCaseBefore, FTPA_RESPONDENT_DOCUMENTS);
        updateFtpaDocDescriptions(asylumCase, updatedAndDeletedDocIds, deletedFtpaDocIds, FTPA_APPELLANT_DOCUMENTS, FTPA_APPELLANT_GROUNDS_DOCUMENTS);
        updateFtpaDocDescriptions(asylumCase, updatedAndDeletedDocIds, deletedFtpaDocIds, FTPA_APPELLANT_DOCUMENTS, FTPA_APPELLANT_EVIDENCE_DOCUMENTS);
        updateFtpaDocDescriptions(asylumCase, updatedAndDeletedDocIds, deletedFtpaDocIds, FTPA_RESPONDENT_DOCUMENTS, FTPA_RESPONDENT_GROUNDS_DOCUMENTS);
        updateFtpaDocDescriptions(asylumCase, updatedAndDeletedDocIds, deletedFtpaDocIds, FTPA_RESPONDENT_DOCUMENTS, FTPA_RESPONDENT_EVIDENCE_DOCUMENTS);
    }

    private void updateFtpaDocDescriptions(AsylumCase asylumCase, List<String> updatedAndDeletedDocIds,
            List<String> deletedFtpaDocIds, AsylumCaseFieldDefinition docTabDocuments, AsylumCaseFieldDefinition ftpaTabDocuments) {
        Set<String> deletedDocIdSet = new HashSet<>(deletedFtpaDocIds);
        Optional<List<IdValue<DocumentWithMetadata>>> optionalFtpaDocuments = asylumCase.read(docTabDocuments);
        Optional<List<IdValue<DocumentWithDescription>>> optionalFtpaDocumentsDescription = asylumCase.read(ftpaTabDocuments);
        optionalFtpaDocuments.ifPresent(ftpaDocuments ->
                optionalFtpaDocumentsDescription.ifPresent(ftpaDocumentsDescription -> {

                    updatedAndDeletedDocIds.forEach(updatedDocId -> {

                        if (deletedDocIdSet.contains(updatedDocId)) {
                            return;
                        }

                        Optional<IdValue<DocumentWithDescription>> optionalOldIdValue =
                                ftpaDocumentsDescription.stream()
                                        .filter(document -> {

                                            Document doc =
                                                    document.getValue()
                                                            .getDocument()
                                                            .orElse(null);

                                            return doc != null
                                                    && getIdFromDocUrl(
                                                    doc.getDocumentUrl()
                                            ).equals(updatedDocId);
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
                        IdValue<DocumentWithDescription> newIdValue = new IdValue<>(oldIdValue.getId(), new DocumentWithDescription(oldIdValue.getValue().getDocument().orElse(null), newDescription));
                        ftpaDocumentsDescription.remove(oldIdValue);
                        ftpaDocumentsDescription.add(newIdValue);
                    });

                    asylumCase.write(ftpaTabDocuments, ftpaDocumentsDescription);
                })
        );
    }

    private void addToUpdatedAndDeletedDocIds(
            List<String> list,
            AsylumCase asylumCase,
            AsylumCase asylumCaseBefore,
            AsylumCaseFieldDefinition documentType
    ) {

        list.addAll(
                docsAuditService.getUpdatedAndDeletedDocIdsForGivenField(
                        asylumCase,
                        asylumCaseBefore,
                        documentType
                )
        );
    }

    private void removeFromUpdatedAndDeletedDocIds(
            List<String> updatedAndDeletedDocIds,
            Optional<List<IdValue<DocumentWithMetadata>>> optionalDocuments
    ) {

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
