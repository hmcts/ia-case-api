package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class EditDocsMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == Event.EDIT_DOCUMENTS;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);

        for (AsylumCaseFieldDefinition field : getFieldDefinitions()) {
            Optional<List<IdValue<DocumentWithMetadata>>> idValuesOptional = asylumCase.read(field);
            if (idValuesOptional.isPresent()) {
                for (IdValue<DocumentWithMetadata> documentWithMetadataIdValue : idValuesOptional.get()) {
                    try {
                        documentWithMetadataIdValue.getValue().getDocument();
                        validateDocumentUploadedDate(documentWithMetadataIdValue);
                    } catch (NullPointerException npe) {
                        asylumCasePreSubmitCallbackResponse.addError("If you add a new document you must complete the fields related to that document including Date uploaded, or remove it, before you can submit your change.");
                        return asylumCasePreSubmitCallbackResponse;
                    }
                }
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void validateDocumentUploadedDate(IdValue<DocumentWithMetadata> documentWithMetadataIdValue) {
        if (Objects.isNull(documentWithMetadataIdValue.getValue().getDateUploaded())
                && Objects.isNull(documentWithMetadataIdValue.getValue().getDateTimeUploaded())) {
            throw new NullPointerException();
        }
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
            ALL_FTPA_APPELLANT_DECISION_DOCS,
            FTPA_APPELLANT_DOCUMENTS,
            ALL_FTPA_RESPONDENT_DECISION_DOCS,
            FTPA_RESPONDENT_DOCUMENTS
        );
    }

}
