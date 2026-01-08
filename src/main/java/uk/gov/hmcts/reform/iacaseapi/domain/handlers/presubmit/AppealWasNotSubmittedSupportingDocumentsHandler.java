package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_NOT_SUBMITTED_REASON_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class AppealWasNotSubmittedSupportingDocumentsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;
    private final FeatureToggler featureToggler;

    public AppealWasNotSubmittedSupportingDocumentsHandler(
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
            && callback.getCaseDetails().getState() != State.APPEAL_STARTED;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        YesOrNo isAdmin = asylumCase.read(IS_ADMIN, YesOrNo.class).orElse(YesOrNo.NO);

        if (featureToggler.getValue("dlrm-internal-feature-flag", false) && isAdmin.equals(YesOrNo.YES)) {

            Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingLegalRepDocuments =
                    asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS);

            List<IdValue<DocumentWithMetadata>> existingLegalRepDocuments =
                    maybeExistingLegalRepDocuments.orElse(emptyList());

            if (HandlerUtils.isAppellantsRepresentation(asylumCase)) {
                asylumCase.write(LEGAL_REPRESENTATIVE_DOCUMENTS, removeAppealNotSubmittedDocument(existingLegalRepDocuments));

                return new PreSubmitCallbackResponse<>(asylumCase);
            }

            Optional<List<IdValue<DocumentWithDescription>>> maybeAppealNotSubmittedReasonDocuments =
                asylumCase.read(APPEAL_NOT_SUBMITTED_REASON_DOCUMENTS);

            List<DocumentWithMetadata> appealWasNotSubmittedReasons =
                maybeAppealNotSubmittedReasonDocuments
                    .orElse(emptyList())
                    .stream()
                    .map(IdValue::getValue)
                    .map(document -> documentReceiver.tryReceive(document, DocumentTag.APPEAL_WAS_NOT_SUBMITTED_SUPPORTING_DOCUMENT))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            if (!appealWasNotSubmittedReasons.isEmpty()) {
                List<IdValue<DocumentWithMetadata>> allLegalRepDocuments =
                    documentsAppender.append(existingLegalRepDocuments, appealWasNotSubmittedReasons, DocumentTag.APPEAL_WAS_NOT_SUBMITTED_SUPPORTING_DOCUMENT);

                asylumCase.write(LEGAL_REPRESENTATIVE_DOCUMENTS, allLegalRepDocuments);
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private static List<IdValue<DocumentWithMetadata>> removeAppealNotSubmittedDocument(List<IdValue<DocumentWithMetadata>> existingLegalRepDocuments) {
        return existingLegalRepDocuments.stream().filter(documentWithMetadataIdValue ->
                documentWithMetadataIdValue.getValue().getTag() != null
                        && !documentWithMetadataIdValue.getValue().getTag()
                        .equals(DocumentTag.APPEAL_WAS_NOT_SUBMITTED_SUPPORTING_DOCUMENT)).collect(Collectors.toList());
    }

}
