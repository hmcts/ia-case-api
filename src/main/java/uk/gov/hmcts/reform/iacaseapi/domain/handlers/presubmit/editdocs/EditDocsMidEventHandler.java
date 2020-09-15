package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.List;
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
                        documentWithMetadataIdValue.getValue().getDateUploaded();
                    } catch (NullPointerException npe) {
                        asylumCasePreSubmitCallbackResponse.addError("If you add a new document you must complete the fields related to that document, or remove it, before you can submit your changes.");
                        return asylumCasePreSubmitCallbackResponse;
                    }
                }
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private List<AsylumCaseFieldDefinition> getFieldDefinitions() {
        return Arrays.asList(
            ADDITIONAL_EVIDENCE_DOCUMENTS,
            TRIBUNAL_DOCUMENTS,
            HEARING_DOCUMENTS,
            LEGAL_REPRESENTATIVE_DOCUMENTS,
            ADDENDUM_EVIDENCE_DOCUMENTS,
            RESPONDENT_DOCUMENTS,
            DRAFT_DECISION_AND_REASONS_DOCUMENTS,
            FINAL_DECISION_AND_REASONS_DOCUMENTS
        );
    }

}
