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
        Optional<List<IdValue<DocumentWithDescription>>> currentDocuments = asylumCase.read(documentType);
        currentDocuments.ifPresent(documentWithDescriptionList -> {
            List<IdValue<DocumentWithDescription>> filteredList = documentWithDescriptionList.stream()
                    .filter(idValue ->
                            !isDeletedDocument(idValue.getValue().getDocument().orElse(null), deletedDocIds)
                    )
                    .toList();
            asylumCase.write(documentType, filteredList);
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
            List<IdValue<DocumentWithMetadata>> filteredDocuments = documents.stream()
                    .filter(idValue ->
                            !isDeletedDocument(
                                    idValue.getValue().getDocument(),
                                    deletedDocIds
                            )
                    )
                    .toList();

            asylumCase.write(documentType, filteredDocuments);

            if (documentType.equals(REASONS_FOR_APPEAL_DOCUMENTS) && filteredDocuments.isEmpty()) {
                asylumCase.clear(REASONS_FOR_APPEAL_DECISION);
                asylumCase.clear(REASONS_FOR_APPEAL_DATE_UPLOADED);
            }
        });
    }

    private void cleanUpFtpaApplicationDocuments(AsylumCase asylumCase, List<String> deletedDocIds) {
        Optional<List<IdValue<FtpaApplications>>> optionalFtpaList = asylumCase.read(FTPA_LIST);
        optionalFtpaList.ifPresent(ftpaList -> {
            List<IdValue<FtpaApplications>> filteredFtpaList = ftpaList.stream()
                .map(ftpaApplicationIdValue -> {
                    FtpaApplications ftpaApplication = ftpaApplicationIdValue.getValue();
                    ftpaApplication.setFtpaOutOfTimeDocuments(
                        filterDocuments(ftpaApplication.getFtpaOutOfTimeDocuments(), deletedDocIds));
                    ftpaApplication.setFtpaGroundsDocuments(
                        filterDocuments(ftpaApplication.getFtpaGroundsDocuments(), deletedDocIds));
                    ftpaApplication.setFtpaEvidenceDocuments(
                        filterDocuments(ftpaApplication.getFtpaEvidenceDocuments(), deletedDocIds));
                    return ftpaApplicationIdValue;
                })
                .filter(ftpaApplicationIdValue -> {
                    FtpaApplications ftpaApplication = ftpaApplicationIdValue.getValue();
                    return !isEmpty(ftpaApplication.getFtpaOutOfTimeDocuments())
                            || !isEmpty(ftpaApplication.getFtpaGroundsDocuments())
                            || !isEmpty(ftpaApplication.getFtpaEvidenceDocuments());
                })
                .toList();
            asylumCase.write(FTPA_LIST, filteredFtpaList);
        });
    }

    private boolean isDeletedDocument(Document document, List<String> deletedDocIds) {
        return document != null
                && deletedDocIds.contains(getIdFromDocUrl(document.getDocumentUrl()));
    }

    private List<IdValue<DocumentWithDescription>> filterDocuments(
            List<IdValue<DocumentWithDescription>> documents,
            List<String> deletedDocIds
    ) {
        if (documents == null || documents.isEmpty()) {
            return documents;
        }

        return documents.stream()
                .filter(idValue ->
                        !isDeletedDocument(
                                idValue.getValue().getDocument().orElse(null),
                                deletedDocIds
                        )
                )
                .toList();
    }

    private List<String> getDeletedAppealDocIds(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        List<String> updatedAndDeletedDocIds =
                docsAuditService.getUpdatedAndDeletedDocIdsForGivenField(
                        asylumCase,
                        asylumCaseBefore,
                        LEGAL_REPRESENTATIVE_DOCUMENTS
                );
        return filterOutCurrentDocIds(
                updatedAndDeletedDocIds,
                asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)
        );
    }

    private List<String> getDeletedFtpaDocIds(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        List<String> updatedAndDeletedDocIdsForGivenField = new ArrayList<>();

        updatedAndDeletedDocIdsForGivenField.addAll(getUpdatedAndDeletedDocIds(asylumCase, asylumCaseBefore, ALL_FTPA_APPELLANT_DECISION_DOCS));
        updatedAndDeletedDocIdsForGivenField.addAll(getUpdatedAndDeletedDocIds(asylumCase, asylumCaseBefore, ALL_FTPA_RESPONDENT_DECISION_DOCS));
        updatedAndDeletedDocIdsForGivenField.addAll(getUpdatedAndDeletedDocIds(asylumCase, asylumCaseBefore, FTPA_APPELLANT_DOCUMENTS));
        updatedAndDeletedDocIdsForGivenField.addAll(getUpdatedAndDeletedDocIds(asylumCase, asylumCaseBefore, FTPA_RESPONDENT_DOCUMENTS));

        List<String> currentFtpaDocIds = new ArrayList<>();
        currentFtpaDocIds.addAll(extractFtpaDocIds(asylumCase.read(ALL_FTPA_APPELLANT_DECISION_DOCS)));
        currentFtpaDocIds.addAll(extractFtpaDocIds(asylumCase.read(ALL_FTPA_RESPONDENT_DECISION_DOCS)));
        currentFtpaDocIds.addAll(extractFtpaDocIds(asylumCase.read(FTPA_APPELLANT_DOCUMENTS)));
        currentFtpaDocIds.addAll(extractFtpaDocIds(asylumCase.read(FTPA_RESPONDENT_DOCUMENTS)));

        return updatedAndDeletedDocIdsForGivenField.stream()
            .filter(id -> !currentFtpaDocIds.contains(id))
            .toList();
    }

    private void updateFtpaNonDecisionDocDescriptions(AsylumCase asylumCase, AsylumCase asylumCaseBefore, List<String> deletedFtpaDocIds) {
        List<String> updatedAndDeletedDocIdsForGivenField = new ArrayList<>();

        updatedAndDeletedDocIdsForGivenField.addAll(getUpdatedAndDeletedDocIds(asylumCase, asylumCaseBefore, FTPA_APPELLANT_DOCUMENTS));
        updatedAndDeletedDocIdsForGivenField.addAll(getUpdatedAndDeletedDocIds(asylumCase, asylumCaseBefore, FTPA_RESPONDENT_DOCUMENTS));
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
        return filterOutCurrentDocIds(
                updatedAndDeletedDocIds,
                asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENTS)
        );
    }

    private List<String> getDeletedFtpaApplicationDocIds(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        List<String> updatedAndDeletedDocIds = new ArrayList<>();
        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIds, asylumCase, asylumCaseBefore, FTPA_APPELLANT_DOCUMENTS);
        addToUpdatedAndDeletedDocIds(updatedAndDeletedDocIds, asylumCase, asylumCaseBefore, FTPA_RESPONDENT_DOCUMENTS);
        List<String> filteredDocIds = filterOutCurrentDocIds(updatedAndDeletedDocIds, asylumCase.read(FTPA_APPELLANT_DOCUMENTS));
        return filterOutCurrentDocIds(filteredDocIds, asylumCase.read(FTPA_RESPONDENT_DOCUMENTS));
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

    private List<String> getUpdatedAndDeletedDocIds(AsylumCase asylumCase, AsylumCase asylumCaseBefore, AsylumCaseFieldDefinition documentType) {
        return docsAuditService.getUpdatedAndDeletedDocIdsForGivenField(asylumCase, asylumCaseBefore, documentType);
    }

    private List<String> extractFtpaDocIds(Optional<List<IdValue<DocumentWithMetadata>>> optionalFtpaDocuments) {
        return optionalFtpaDocuments
            .map(this::getFtpaDocIds)
            .orElse(List.of());
    }

    private List<String> getFtpaDocIds(List<IdValue<DocumentWithMetadata>> documents) {
        return documents.stream()
            .map(idValue -> getIdFromDocUrl(idValue.getValue().getDocument().getDocumentUrl()))
            .toList();
    }

    private void updateFtpaDocDescriptions(
            AsylumCase asylumCase,
            List<String> updatedAndDeletedDocIds,
            List<String> deletedFtpaDocIds,
            AsylumCaseFieldDefinition docTabDocuments,
            AsylumCaseFieldDefinition ftpaTabDocuments
    ) {
        Optional<List<IdValue<DocumentWithMetadata>>> optionalFtpaDocuments = asylumCase.read(docTabDocuments);
        Optional<List<IdValue<DocumentWithDescription>>> optionalFtpaDocumentsDescription = asylumCase.read(ftpaTabDocuments);

        optionalFtpaDocuments.ifPresent(ftpaDocuments -> optionalFtpaDocumentsDescription.ifPresent(
                ftpaDocumentsDescription -> {
                    List<IdValue<DocumentWithDescription>> updatedList = ftpaDocumentsDescription.stream()
                        .map(idValue -> {
                            String docId = getIdFromDocUrl(idValue.getValue().getDocument().orElseThrow().getDocumentUrl());

                            if (updatedAndDeletedDocIds.contains(docId) && !deletedFtpaDocIds.contains(docId)) {
                                String newDescription = ftpaDocuments.stream()
                                        .filter(document -> getIdFromDocUrl(
                                                document.getValue().getDocument().getDocumentUrl()).equals(docId))
                                        .findFirst()
                                        .map(doc -> doc.getValue().getDescription())
                                        .orElse(idValue.getValue().getDescription().orElse(null));
                                return new IdValue<>(idValue.getId(), new DocumentWithDescription(idValue.getValue().getDocument().orElseThrow(), newDescription));
                            }
                            return idValue;
                        }).toList();
                    asylumCase.write(ftpaTabDocuments, updatedList);
                }));
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

    private List<String> filterOutCurrentDocIds(
            List<String> updatedAndDeletedDocIds,
            Optional<List<IdValue<DocumentWithMetadata>>> optionalDocuments
    ) {
        if (optionalDocuments.isEmpty()) {
            return updatedAndDeletedDocIds;
        }
        List<String> currentDocIds = optionalDocuments.get().stream()
                .map(idValue ->
                        getIdFromDocUrl(
                                idValue.getValue()
                                        .getDocument()
                                        .getDocumentUrl()
                        )
                )
                .toList();
        return updatedAndDeletedDocIds.stream()
                .filter(docId -> !currentDocIds.contains(docId))
                .toList();
    }
}
