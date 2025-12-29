package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class EditBailApplicationAfterSubmitHandler implements PreSubmitCallbackHandler<BailCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public EditBailApplicationAfterSubmitHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT;
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

        final Optional<List<IdValue<DocumentWithMetadata>>> maybeApplicantDocuments =
            bailCase.read(APPLICANT_DOCUMENTS_WITH_METADATA);

        if (maybeApplicantDocuments.isPresent()) {
            List<IdValue<DocumentWithMetadata>> applicantDocumentsList = maybeApplicantDocuments.get();

            List<IdValue<DocumentWithMetadata>> updatedApplicantDocumentsList =
                applicantDocumentsList
                    .stream()
                    .filter(documentWithMetaData ->
                                !documentWithMetaData.getValue().getTag().equals(DocumentTag.B1_DOCUMENT)
                                && !documentWithMetaData.getValue().getTag().equals(DocumentTag.BAIL_EVIDENCE)
                                && !documentWithMetaData.getValue().getTag().equals(DocumentTag.BAIL_SUBMISSION))
                    .collect(Collectors.toList());

            bailCase.write(APPLICANT_DOCUMENTS_WITH_METADATA, updatedApplicantDocumentsList);
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
