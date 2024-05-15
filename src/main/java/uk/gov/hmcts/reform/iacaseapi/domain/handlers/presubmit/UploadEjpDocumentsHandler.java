package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.IAUT_2_FORM;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag.UPPER_TRIBUNAL_TRANSFER_ORDER_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isEjpCase;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
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
public class UploadEjpDocumentsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;
    private final String utTransferOrderSuffix = "UT-transfer-order";
    private final String iaut2AppealFormSuffix = "IAUT-2-appeal-form";

    public UploadEjpDocumentsHandler(
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

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && isInternalCase(asylumCase)
                && isEjpCase(asylumCase)
                && callback.getEvent() == Event.SUBMIT_APPEAL;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String appellantName = asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse("");
        String appealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse("");

        Optional<List<IdValue<Document>>> maybeUtOrderDocs = asylumCase.read(UT_TRANSFER_DOC);
        List<IdValue<Document>> utOrderDocs =
                maybeUtOrderDocs.orElseThrow(() -> new IllegalStateException("utTransferDoc is not present"));

        Optional<List<IdValue<Document>>> maybeEjpAppealFormDocs = asylumCase.read(UPLOAD_EJP_APPEAL_FORM_DOCS);
        List<IdValue<Document>> ejpAppealFormDocs =
                maybeEjpAppealFormDocs.orElseThrow(() -> new IllegalStateException("uploadEjpAppealFormDocs is not present"));

        renameDocuments(utOrderDocs, appellantName, appealReferenceNumber, utTransferOrderSuffix);
        renameDocuments(ejpAppealFormDocs, appellantName, appealReferenceNumber, iaut2AppealFormSuffix);

        List<DocumentWithMetadata> documentsWithMetadata = new ArrayList<>();
        documentsWithMetadata.addAll(fetchDocumentWithMetadata(utOrderDocs, UPPER_TRIBUNAL_TRANSFER_ORDER_DOCUMENT));
        documentsWithMetadata.addAll(fetchDocumentWithMetadata(ejpAppealFormDocs, IAUT_2_FORM));

        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingTribunalDocuments =
                asylumCase.read(TRIBUNAL_DOCUMENTS);

        List<IdValue<DocumentWithMetadata>> existingTribunalDocuments =
                maybeExistingTribunalDocuments.orElse(emptyList());

        if (!documentsWithMetadata.isEmpty()) {
            List<IdValue<DocumentWithMetadata>> allTribunalDocuments =
                    documentsAppender.prepend(existingTribunalDocuments, documentsWithMetadata);
            asylumCase.write(TRIBUNAL_DOCUMENTS, allTribunalDocuments);
        }

        asylumCase.write(UT_TRANSFER_DOC, utOrderDocs);
        asylumCase.write(UPLOAD_EJP_APPEAL_FORM_DOCS, ejpAppealFormDocs);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void renameDocuments(List<IdValue<Document>> listOfDocToRename,
                                                    String appellantName, String appealReferenceNumber,
                                                    String docSuffixName) {
        int docNum = 0;
        for (IdValue<Document> doc : listOfDocToRename) {
            docNum++;

            int finalDocNum = docNum;

            String fileExtension = FilenameUtils.getExtension(doc.getValue().getDocumentFilename());
            doc.getValue().setDocumentFilename(appealReferenceNumber
                    + "-"
                    + appellantName
                    + "-"
                    + docSuffixName
                    + finalDocNum
                    + "."
                    + fileExtension);
        }
    }
    
    private List<DocumentWithMetadata> fetchDocumentWithMetadata(List<IdValue<Document>> docsToPrepend,
                                                                 DocumentTag documentTag) {
        return docsToPrepend
                .stream()
                .map(IdValue::getValue)
                .map(document -> documentReceiver.receive(document, "", documentTag))
                .collect(Collectors.toList());
    }
}
