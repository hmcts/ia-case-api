package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPLOAD_SENSITIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;


@Component
public class UploadSensitiveDocsAboutToSubmitHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public UploadSensitiveDocsAboutToSubmitHandler(DocumentReceiver documentReceiver,
                                                   DocumentsAppender documentsAppender) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return ABOUT_TO_SUBMIT.equals(callbackStage) && UPLOAD_SENSITIVE_DOCUMENTS.equals(callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        List<IdValue<DocumentWithMetadata>> allSensitiveDocs = addNewSensitiveDocsToExistingOnes(asylumCase);

        asylumCase.write(UPLOAD_SENSITIVE_DOCS, allSensitiveDocs);

        asylumCase.clear(UPLOAD_SENSITIVE_DOCS_FILE_UPLOADS);
        asylumCase.clear(UPLOAD_SENSITIVE_DOCS_IS_APPELLANT_RESPONDENT);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private List<IdValue<DocumentWithMetadata>> addNewSensitiveDocsToExistingOnes(AsylumCase asylumCase) {
        final List<IdValue<DocumentWithMetadata>> existingSensitiveDocs =
            getDocumentsForGivenField(asylumCase, UPLOAD_SENSITIVE_DOCS);

        List<DocumentWithMetadata> newSensitiveDocs = getNewSensitiveDocs(asylumCase);

        return documentsAppender.append(existingSensitiveDocs, newSensitiveDocs);
    }

    private List<DocumentWithMetadata> getNewSensitiveDocs(AsylumCase asylumCase) {
        final List<IdValue<DocumentWithDescription>> newUploadedSensitiveDocs =
            getDocumentsForGivenField(asylumCase, UPLOAD_SENSITIVE_DOCS_FILE_UPLOADS);

        String suppliedBy = asylumCase.read(UPLOAD_SENSITIVE_DOCS_IS_APPELLANT_RESPONDENT, String.class)
            .orElse(StringUtils.EMPTY);

        return new ArrayList<>(documentReceiver
            .tryReceiveAll(newUploadedSensitiveDocs, DocumentTag.SENSITIVE_DOCUMENT, suppliedBy));
    }

    private <T> List<IdValue<T>> getDocumentsForGivenField(AsylumCase asylumCase,
                                                           AsylumCaseFieldDefinition field) {
        final Optional<List<IdValue<T>>> docsOptional = asylumCase.read(field);
        return docsOptional.orElse(Collections.emptyList());
    }
}
