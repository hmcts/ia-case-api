package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDENDUM_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DRAFT_DECISION_AND_REASONS_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FINAL_DECISION_AND_REASONS_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.TRIBUNAL_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.NONE;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class EditDocsAboutToSubmitHandler implements PreSubmitCallbackHandler<AsylumCase> {
    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.EDIT_DOCUMENTS && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        restoreDocumentTagForDocs(callback, asylumCase);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void restoreDocumentTagForDocs(Callback<AsylumCase> callback, AsylumCase asylumCase) {
        getFieldDefAndFallbackDocumentTagPairs().forEach(
            pair -> restoreDocumentTagValue(callback, asylumCase, pair.getKey(), pair.getValue()));
    }

    private void restoreDocumentTagValue(Callback<AsylumCase> callback, AsylumCase asylumCase,
                                         AsylumCaseFieldDefinition fieldDefinition, DocumentTag fallbackDocumentTag) {
        Optional<List<IdValue<DocumentWithMetadata>>> idValuesOptional = asylumCase.read(fieldDefinition);
        idValuesOptional.ifPresent(idValues -> {
            List<IdValue<DocumentWithMetadata>> idValuesBefore = getIdValuesBefore(callback, fieldDefinition);
            asylumCase.write(fieldDefinition,
                getIdValuesWithBeforeDocumentTags(idValues, idValuesBefore, fallbackDocumentTag));
        });
    }

    private List<AbstractMap.SimpleImmutableEntry<AsylumCaseFieldDefinition, DocumentTag>> getFieldDefAndFallbackDocumentTagPairs() {
        return Arrays.asList(
            new AbstractMap.SimpleImmutableEntry<>(ADDITIONAL_EVIDENCE_DOCUMENTS, NONE),
            new AbstractMap.SimpleImmutableEntry<>(TRIBUNAL_DOCUMENTS, NONE),
            new AbstractMap.SimpleImmutableEntry<>(HEARING_DOCUMENTS, NONE),
            new AbstractMap.SimpleImmutableEntry<>(LEGAL_REPRESENTATIVE_DOCUMENTS, NONE),
            new AbstractMap.SimpleImmutableEntry<>(ADDENDUM_EVIDENCE_DOCUMENTS, NONE),
            new AbstractMap.SimpleImmutableEntry<>(RESPONDENT_DOCUMENTS, NONE),
            new AbstractMap.SimpleImmutableEntry<>(DRAFT_DECISION_AND_REASONS_DOCUMENTS, NONE),
            new AbstractMap.SimpleImmutableEntry<>(FINAL_DECISION_AND_REASONS_DOCUMENTS, NONE)
        );
    }

    private List<IdValue<DocumentWithMetadata>> getIdValuesBefore(Callback<AsylumCase> callback,
                                                                  AsylumCaseFieldDefinition fieldDefinition) {
        Optional<CaseDetails<AsylumCase>> asylumCaseBeforeOptional = callback.getCaseDetailsBefore();
        if (asylumCaseBeforeOptional.isPresent()) {
            CaseDetails<AsylumCase> asylumCaseBefore = asylumCaseBeforeOptional.get();
            Optional<List<IdValue<DocumentWithMetadata>>> idValuesBeforeOptional = asylumCaseBefore.getCaseData()
                .read(fieldDefinition);
            if (idValuesBeforeOptional.isPresent()) {
                return idValuesBeforeOptional.get();
            }
        }
        return Collections.emptyList();
    }

    private List<IdValue<DocumentWithMetadata>> getIdValuesWithBeforeDocumentTags(
        List<IdValue<DocumentWithMetadata>> idValues, List<IdValue<DocumentWithMetadata>> idValuesBefore,
        DocumentTag fallbackDocumentTag) {
        List<IdValue<DocumentWithMetadata>> idValuesWithBeforeTag = new ArrayList<>();
        idValues.forEach(idValue -> {
            DocumentWithMetadata docWithBeforeTags = buildDocWithBeforeTags(idValuesBefore, idValue,
                fallbackDocumentTag);
            IdValue<DocumentWithMetadata> idValueWithBeforeTag = new IdValue<>(idValue.getId(), docWithBeforeTags);
            idValuesWithBeforeTag.add(idValueWithBeforeTag);
        });
        return idValuesWithBeforeTag;
    }

    private DocumentWithMetadata buildDocWithBeforeTags(List<IdValue<DocumentWithMetadata>> idValuesBefore,
                                                        IdValue<DocumentWithMetadata> idValue,
                                                        DocumentTag fallbackDocumentTag) {
        DocumentWithMetadata value = idValue.getValue();
        DocumentTag beforeTagForGivenIdValue = getBeforeTagForGivenIdValue(idValue.getId(), idValuesBefore,
            fallbackDocumentTag);
        return new DocumentWithMetadata(value.getDocument(),
            value.getDescription(), value.getDateUploaded(), beforeTagForGivenIdValue, value.getSuppliedBy());
    }

    private DocumentTag getBeforeTagForGivenIdValue(String idValueId,
                                                    List<IdValue<DocumentWithMetadata>> fieldBeforeList,
                                                    DocumentTag fallbackDocumentTag) {
        return fieldBeforeList.stream()
            .filter(beforeDoc -> beforeDoc.getId().equals(idValueId))
            .findFirst()
            .orElse(getIdValueWithDefaultTag(idValueId, fallbackDocumentTag)).getValue().getTag();
    }

    private IdValue<DocumentWithMetadata> getIdValueWithDefaultTag(String idValueId, DocumentTag documentTag) {
        DocumentWithMetadata docMeta = new DocumentWithMetadata(null, null, null,
            documentTag);
        return new IdValue<>(idValueId, docMeta);
    }
}
