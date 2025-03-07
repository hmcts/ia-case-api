package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class UploadSignedDecisionNoticeHandler implements PreSubmitCallbackHandler<BailCase> {

    private final DateProvider dateProvider;

    public UploadSignedDecisionNoticeHandler(
            DateProvider dateProvider
    ) {
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPLOAD_SIGNED_DECISION_NOTICE;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase =
                callback
                        .getCaseDetails()
                        .getCaseData();
        // For cases prior to RIA-6280, the unsigned document was saved in Tribunal Documents.
        // For such cases, when we do upload signed decision, we still want to check if
        // document with tag BAIL_DECISION_UNSIGNED is present, if so, remove it from the Tribunal Collection.
        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingTribunalDocuments =
                bailCase.read(TRIBUNAL_DOCUMENTS_WITH_METADATA);

        final List<IdValue<DocumentWithMetadata>> existingTribunalDocuments =
                maybeExistingTribunalDocuments.orElse(Collections.emptyList());

        List<IdValue<DocumentWithMetadata>> allTribunalDocuments = new ArrayList<>(existingTribunalDocuments);
        allTribunalDocuments
                .removeIf(document -> document.getValue().getTag().equals(DocumentTag.BAIL_DECISION_UNSIGNED));
        bailCase.write(TRIBUNAL_DOCUMENTS_WITH_METADATA, allTribunalDocuments);

        // By this point, we have already cleared the fields UNSIGNED_DECISION_DOCUMENTS_WITH_METADATA
        // in documents-api after pdf conversion.
        // The converted pdf is placed in UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT
        // & SIGNED_DECISION_DOCUMENT_WITH_METADATA.
        bailCase.write(OUTCOME_DATE, dateProvider.nowWithTime().toString());
        bailCase.write(OUTCOME_STATE, State.DECISION_DECIDED);
        bailCase.write(HAS_BEEN_RELISTED, YesOrNo.NO);
        bailCase.clear(DECISION_UNSIGNED_DOCUMENT);

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
