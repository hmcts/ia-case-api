package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FINAL_DECISION_AND_REASONS_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FINAL_DECISION_AND_REASONS_PDF;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs.EditDocsAuditService.getIdFromDocUrl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
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
        if (currentFinalDecisionAndReasonPdf != null
            && doWeHaveToCleanUpOverviewTabDoc(deletedFinalDecisionAndReasonsDocIds, currentFinalDecisionAndReasonPdf)) {
            asylumCase.clear(FINAL_DECISION_AND_REASONS_PDF);
        }
    }

    private boolean doWeHaveToCleanUpOverviewTabDoc(List<String> deletedFinalDecisionAndReasonsDocIds,
                                                    Document currentFinalDecisionAndReasonPdf) {
        String currentFinalDecisionAndReasonPdfId = getIdFromDocUrl(currentFinalDecisionAndReasonPdf.getDocumentUrl());
        return deletedFinalDecisionAndReasonsDocIds.contains(currentFinalDecisionAndReasonPdfId);
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
