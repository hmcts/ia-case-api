package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class UploadAdditionalEvidenceHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;
    private final FeatureToggler featureToggler;

    public UploadAdditionalEvidenceHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender,
        FeatureToggler featureToggler
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.UPLOAD_ADDITIONAL_EVIDENCE;
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

        Optional<List<IdValue<DocumentWithDescription>>> maybeAdditionalEvidence =
                asylumCase.read(ADDITIONAL_EVIDENCE);

        List<DocumentWithMetadata> additionalEvidenceDocuments =
            maybeAdditionalEvidence
                .orElseThrow(() -> new IllegalStateException("additionalEvidence is not present"))
                .stream()
                .map(IdValue::getValue)
                .map(document -> documentReceiver.tryReceive(document, DocumentTag.ADDITIONAL_EVIDENCE))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingAdditionalEvidenceDocuments =
                asylumCase.read(ADDITIONAL_EVIDENCE_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> existingAdditionalEvidenceDocuments =
            maybeExistingAdditionalEvidenceDocuments.orElse(Collections.emptyList());

        List<IdValue<DocumentWithMetadata>> allAdditionalEvidenceDocuments =
            documentsAppender.append(existingAdditionalEvidenceDocuments, additionalEvidenceDocuments);

        if (asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)
            && featureToggler.getValue("reheard-feature", false)) {

            Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingAdditionalEvidenceRespondentDocuments =
                asylumCase.read(APP_ADDITIONAL_EVIDENCE_DOCS);

            final List<IdValue<DocumentWithMetadata>> existingAdditionalEvidenceRespondentDocuments =
                maybeExistingAdditionalEvidenceRespondentDocuments.orElse(Collections.emptyList());

            List<IdValue<DocumentWithMetadata>> allRespondentDocuments =
                documentsAppender.append(existingAdditionalEvidenceRespondentDocuments, additionalEvidenceDocuments);

            asylumCase.write(APP_ADDITIONAL_EVIDENCE_DOCS, allRespondentDocuments);
        }
        asylumCase.write(ADDITIONAL_EVIDENCE_DOCUMENTS, allAdditionalEvidenceDocuments);

        asylumCase.clear(ADDITIONAL_EVIDENCE);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
