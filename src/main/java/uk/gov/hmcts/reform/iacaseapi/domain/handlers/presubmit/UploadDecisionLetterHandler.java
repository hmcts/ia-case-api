package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;

@Component
public class UploadDecisionLetterHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public UploadDecisionLetterHandler(
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

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final Optional<Document> noticeOfDecisionDocumentOptional = asylumCase
            .read(UPLOAD_THE_NOTICE_OF_DECISION_DOCUMENT, Document.class);

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.SUBMIT_APPEAL
            && noticeOfDecisionDocumentOptional.isPresent();
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        List<DocumentWithMetadata> newLegalRepresentativeDocument = buildNewLegalRepDocument(asylumCase);
        List<IdValue<DocumentWithMetadata>> allLegalRepDocuments =
            addNewLegalRepDocumentToExistingLegalRepDocuments(asylumCase, newLegalRepresentativeDocument);

        asylumCase.write(LEGAL_REPRESENTATIVE_DOCUMENTS, allLegalRepDocuments);
        asylumCase.clear(UPLOAD_THE_NOTICE_OF_DECISION_DOCUMENT);
        asylumCase.clear(UPLOAD_THE_NOTICE_OF_DECISION_EXPLANATION);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private List<IdValue<DocumentWithMetadata>> addNewLegalRepDocumentToExistingLegalRepDocuments(
        AsylumCase asylumCase,
        List<DocumentWithMetadata> newLegalRepresentativeDocument) {

        Optional<List<IdValue<DocumentWithMetadata>>> maybeLegalRepresentativeDocuments =
            asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> legalRepresentativeDocuments =
            maybeLegalRepresentativeDocuments.orElse(emptyList());

        return documentsAppender.append(legalRepresentativeDocuments, newLegalRepresentativeDocument
        );
    }

    private List<DocumentWithMetadata> buildNewLegalRepDocument(AsylumCase asylumCase) {
        Document uploadTheNoticeOfDecisionDocument = asylumCase
            .read(UPLOAD_THE_NOTICE_OF_DECISION_DOCUMENT, Document.class)
            .orElseThrow(() -> new IllegalStateException("uploadTheNoticeOfDecisionDocument is not present"));

        final String uploadTheNoticeOfDecisionExplanation = asylumCase
            .read(UPLOAD_THE_NOTICE_OF_DECISION_EXPLANATION, String.class)
            .orElse("");

        return Collections.singletonList(
            documentReceiver
                .receive(
                    uploadTheNoticeOfDecisionDocument,
                    uploadTheNoticeOfDecisionExplanation,
                    DocumentTag.HO_DECISION_LETTER
                )
        );
    }
}
