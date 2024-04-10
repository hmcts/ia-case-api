package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.IAUT_2_FORM;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.UPPER_TRIBUNAL_TRANSFER_ORDER_DOCUMENT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class EditDocsAboutToSubmitHandler implements PreSubmitCallbackHandler<AsylumCase> {

    
    @Autowired
    private EditDocsCaseNoteService editDocsCaseNoteService;
    @Autowired
    private EditDocsService editDocService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.EDIT_DOCUMENTS && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        CaseDetails<AsylumCase> caseDetailsBefore = callback.getCaseDetailsBefore().orElse(null);
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        AsylumCase asylumCaseBefore = caseDetailsBefore == null ? null : caseDetailsBefore.getCaseData();
        long caseId = callback.getCaseDetails().getId();
        restoreDocumentTagForDocs(asylumCase, asylumCaseBefore);
        editDocsCaseNoteService.writeAuditCaseNoteForGivenCaseId(caseId, asylumCase, asylumCaseBefore);
        editDocService.cleanUpOverviewTabDocs(asylumCase, asylumCaseBefore);
        updateEjpDocumentFields(asylumCase);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void restoreDocumentTagForDocs(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {
        getFieldDefinitions().forEach(field -> {
            Optional<List<IdValue<DocumentWithMetadata>>> idValuesOptional = asylumCase.read(field);
            idValuesOptional.ifPresent(idValues -> {
                List<IdValue<DocumentWithMetadata>> idValuesBefore = getIdValuesBefore(asylumCaseBefore, field);
                asylumCase.write(field, getIdValuesWithBeforeDocumentTags(idValues, idValuesBefore));
            });
        });
    }

    private List<AsylumCaseFieldDefinition> getFieldDefinitions() {
        return Arrays.asList(
            ADDITIONAL_EVIDENCE_DOCUMENTS,
            TRIBUNAL_DOCUMENTS,
            REHEARD_HEARING_DOCUMENTS,
            HEARING_DOCUMENTS,
            LEGAL_REPRESENTATIVE_DOCUMENTS,
            ADDENDUM_EVIDENCE_DOCUMENTS,
            RESPONDENT_DOCUMENTS,
            DRAFT_DECISION_AND_REASONS_DOCUMENTS,
            FINAL_DECISION_AND_REASONS_DOCUMENTS,
            UPLOAD_SENSITIVE_DOCS,
            ALL_FTPA_APPELLANT_DECISION_DOCS,
            FTPA_APPELLANT_DOCUMENTS,
            ALL_FTPA_RESPONDENT_DECISION_DOCS,
            FTPA_RESPONDENT_DOCUMENTS
        );
    }

    private List<IdValue<DocumentWithMetadata>> getIdValuesBefore(AsylumCase asylumCaseBefore,
                                                                  AsylumCaseFieldDefinition fieldDefinition) {
        if (asylumCaseBefore != null) {
            Optional<List<IdValue<DocumentWithMetadata>>> idValuesBeforeOptional = asylumCaseBefore
                .read(fieldDefinition);
            if (idValuesBeforeOptional.isPresent()) {
                return idValuesBeforeOptional.get();
            }
        }
        return Collections.emptyList();
    }

    private List<IdValue<DocumentWithMetadata>> getIdValuesWithBeforeDocumentTags(
        List<IdValue<DocumentWithMetadata>> idValues, List<IdValue<DocumentWithMetadata>> idValuesBefore) {
        List<IdValue<DocumentWithMetadata>> idValuesWithBeforeTag = new ArrayList<>();
        idValues.forEach(idValue -> {
            DocumentWithMetadata docWithBeforeTags = buildDocWithBeforeTags(idValuesBefore, idValue);
            IdValue<DocumentWithMetadata> idValueWithBeforeTag = new IdValue<>(idValue.getId(), docWithBeforeTags);
            idValuesWithBeforeTag.add(idValueWithBeforeTag);
        });
        return idValuesWithBeforeTag;
    }

    private DocumentWithMetadata buildDocWithBeforeTags(List<IdValue<DocumentWithMetadata>> idValuesBefore,
                                                        IdValue<DocumentWithMetadata> idValue) {
        DocumentWithMetadata value = idValue.getValue();
        DocumentTag beforeTagForGivenIdValue = getBeforeTagForGivenIdValue(idValue.getId(), idValuesBefore);
        return new DocumentWithMetadata(value.getDocument(),
            value.getDescription() == null ? "" : value.getDescription(), value.getDateUploaded(),
            beforeTagForGivenIdValue, value.getSuppliedBy());
    }

    private DocumentTag getBeforeTagForGivenIdValue(String idValueId,
                                                    List<IdValue<DocumentWithMetadata>> fieldBeforeList) {
        return fieldBeforeList.stream()
            .filter(beforeDoc -> beforeDoc.getId().equals(idValueId))
            .findFirst()
            .orElse(getIdValueWithDefaultTag(idValueId)).getValue().getTag();
    }

    private IdValue<DocumentWithMetadata> getIdValueWithDefaultTag(String idValueId) {
        DocumentWithMetadata docMeta = new DocumentWithMetadata(null, null, null,
            DocumentTag.NONE);
        return new IdValue<>(idValueId, docMeta);
    }

    private void updateEjpDocumentFields(AsylumCase asylumCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingTribunalDocuments =
                asylumCase.read(TRIBUNAL_DOCUMENTS);

        List<IdValue<DocumentWithMetadata>> existingTribunalDocuments =
                maybeExistingTribunalDocuments.orElse(emptyList());

        List<Document> updatedUtTransferOrderValues =
                fetchDocumentsByTag(existingTribunalDocuments, UPPER_TRIBUNAL_TRANSFER_ORDER_DOCUMENT);
        writeEjpDocuments(asylumCase, UT_TRANSFER_DOC, updatedUtTransferOrderValues);

        List<Document> updatedIaut2Values =
                fetchDocumentsByTag(existingTribunalDocuments, IAUT_2_FORM);
        writeEjpDocuments(asylumCase, UPLOAD_EJP_APPEAL_FORM_DOCS, updatedIaut2Values);

    }

    private void writeEjpDocuments(AsylumCase asylumCase,
                                    AsylumCaseFieldDefinition ejpDocumentField,
                                    List<Document> updatedValues) {
        if (updatedValues.isEmpty()) {
            asylumCase.clear(ejpDocumentField);
        } else {
            List<IdValue<Document>> allDocs = new ArrayList<>();
            int index = updatedValues.size();

            for (Document value : updatedValues) {
                allDocs.add(new IdValue<>(String.valueOf(index--), value));
            }
            asylumCase.write(ejpDocumentField, allDocs);
        }
    }

    private List<Document> fetchDocumentsByTag(List<IdValue<DocumentWithMetadata>> existingTribunalDocuments,
                                               DocumentTag documentTag) {
        return existingTribunalDocuments.stream()
                .filter(d -> d.getValue().getTag().equals(documentTag))
                .map(v -> v.getValue().getDocument())
                .collect(Collectors.toList());
    }
}
