package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.EditDocsAuditService.getIdFromDocUrl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.EditDocsAuditService;

@Service
public class EditDocsService {

    @Autowired
    private EditDocsAuditService docsAuditService;

    public void cleanUpOverviewTabDocs(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        List<String> deletedFinalDecisionAndReasonsDocIds = getDeletedDocIds(asylumCase, asylumCaseBefore);
        Document currentFinalDecisionAndReasonPdf = asylumCase.read(FINAL_DECISION_AND_REASONS_PDF, Document.class)
                .orElse(null);
        Optional<List<IdValue<DocumentWithDescription>>> currentFTPAAppellantDecisionAndReasonDocuments = asylumCase.read(FTPA_APPELLANT_DECISION_DOCUMENT);
        Optional<List<IdValue<DocumentWithDescription>>>  currentFTPARespondentDecisionAndReasonDocuments = asylumCase.read(FTPA_RESPONDENT_DECISION_DOCUMENT);

        if (currentFinalDecisionAndReasonPdf != null
                && doWeHaveToCleanUpOverviewTabDoc(deletedFinalDecisionAndReasonsDocIds, currentFinalDecisionAndReasonPdf)) {
            asylumCase.clear(FINAL_DECISION_AND_REASONS_PDF);
        }

        currentFTPAAppellantDecisionAndReasonDocuments.ifPresent(documentWithDescriptionList -> {
            documentWithDescriptionList.removeIf(idValue ->
                    doWeHaveToCleanUpOverviewTabFTPADoc(deletedFinalDecisionAndReasonsDocIds, idValue.getId())
            );
            asylumCase.write(FTPA_APPELLANT_DECISION_DOCUMENT, documentWithDescriptionList);
        });

        currentFTPARespondentDecisionAndReasonDocuments.ifPresent(documentWithDescriptionList -> {
            documentWithDescriptionList.removeIf(idValue ->
                    doWeHaveToCleanUpOverviewTabFTPADoc(deletedFinalDecisionAndReasonsDocIds, idValue.getId())
            );
            asylumCase.write(FTPA_RESPONDENT_DECISION_DOCUMENT, documentWithDescriptionList);
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
            .collect(Collectors.toList());
    }

}
