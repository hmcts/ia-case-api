package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDENDUM_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDENDUM_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_APPELLANT_RESPONDENT;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class UploadAddendumEvidenceHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public UploadAddendumEvidenceHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        List<IdValue<DocumentWithDescription>> addendumEvidence =
                asylumCase
                    .<List<IdValue<DocumentWithDescription>>>read(ADDENDUM_EVIDENCE)
                    .orElseThrow(() -> new IllegalStateException("additionalEvidence is not present"));

        String appelantRespodent =
                asylumCase
                    .<String>read(IS_APPELLANT_RESPONDENT)
                    .orElseThrow(() -> new IllegalStateException("isAppellantRespondent is not present"));

        List<DocumentWithMetadata> addendumEvidenceDocuments =
            addendumEvidence
                .stream()
                .map(IdValue::getValue)
                .map(document -> documentReceiver.tryReceive(document, DocumentTag.ADDENDUM_EVIDENCE, appelantRespodent))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingAdditionalEvidenceDocuments =
                asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> existingAdditionalEvidenceDocuments =
            maybeExistingAdditionalEvidenceDocuments.orElse(Collections.emptyList());

        List<IdValue<DocumentWithMetadata>> allAddendumEvidenceDocuments =
            documentsAppender.append(existingAdditionalEvidenceDocuments, addendumEvidenceDocuments);

        asylumCase.write(ADDENDUM_EVIDENCE_DOCUMENTS, allAddendumEvidenceDocuments);

        asylumCase.clear(ADDENDUM_EVIDENCE);
        asylumCase.clear(IS_APPELLANT_RESPONDENT);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
