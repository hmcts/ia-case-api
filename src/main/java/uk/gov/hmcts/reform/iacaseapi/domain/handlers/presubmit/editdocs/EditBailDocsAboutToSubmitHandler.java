package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.EditDocsCaseNoteService;

@Component
public class EditBailDocsAboutToSubmitHandler implements PreSubmitCallbackHandler<BailCase> {


    @Autowired
    private EditDocsCaseNoteService editDocsCaseNoteService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.EDIT_BAIL_DOCUMENTS
               && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage,
                                                      Callback<BailCase> callback) {

        CaseDetails<BailCase> caseDetailsBefore = callback.getCaseDetailsBefore().orElse(null);
        BailCase bailCase = callback.getCaseDetails().getCaseData();
        BailCase bailCaseBefore = caseDetailsBefore == null ? null : caseDetailsBefore.getCaseData();
        long caseId = callback.getCaseDetails().getId();
        restoreDocumentTagForDocs(bailCase, bailCaseBefore);
        editDocsCaseNoteService.writeAuditCaseNoteForGivenCaseId(caseId, bailCase, bailCaseBefore);

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private void restoreDocumentTagForDocs(BailCase bailCase, BailCase bailCaseBefore) {
        getFieldDefinitions().forEach(field -> {
            Optional<List<IdValue<DocumentWithMetadata>>> idValuesOptional = bailCase.read(field);
            idValuesOptional.ifPresent(idValues -> {
                List<IdValue<DocumentWithMetadata>> idValuesBefore = getIdValuesBefore(bailCaseBefore, field);
                bailCase.write(field, getIdValuesWithBeforeDocumentTags(idValues, idValuesBefore));
            });
        });
    }

    private List<BailCaseFieldDefinition> getFieldDefinitions() {
        return Arrays.asList(
            TRIBUNAL_DOCUMENTS_WITH_METADATA,
            HOME_OFFICE_DOCUMENTS_WITH_METADATA,
            APPLICANT_DOCUMENTS_WITH_METADATA);
    }

    private List<IdValue<DocumentWithMetadata>> getIdValuesBefore(BailCase bailCaseBefore,
                                                                  BailCaseFieldDefinition fieldDefinition) {
        if (bailCaseBefore != null) {
            Optional<List<IdValue<DocumentWithMetadata>>> idValuesBeforeOptional = bailCaseBefore
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
                                        value.getDescription() == null ? "" : value.getDescription(),
                                                                                value.getDateUploaded(),
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
}
