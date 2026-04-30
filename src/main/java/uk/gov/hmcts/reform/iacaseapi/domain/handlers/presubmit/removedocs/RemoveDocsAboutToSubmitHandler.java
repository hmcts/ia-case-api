package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.removedocs;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class RemoveDocsAboutToSubmitHandler implements PreSubmitCallbackHandler<AsylumCase> {

    @Autowired
    private RemoveDocsService removeDocService;

    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.REMOVE_DOCUMENTS && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        return new PreSubmitCallbackResponse<>(asylumCase);
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


}
